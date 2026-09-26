package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dao.FileTabRepository;

class EncryptingRepositoryTest {

  private static final char[] PASSWORD = "secret".toCharArray();

  @Test
  void protectedNotesAreEncryptedOnDiskAndReadableWithTheKey(@TempDir Path data) {
    FileTabRepository disk = new FileTabRepository(data);
    Keyring keyring = new Keyring();
    EncryptingRepository repo = new EncryptingRepository(disk, keyring);

    repo.protect("a", "A", "my secret", NoteCrypto.newKey(PASSWORD));
    repo.save("a", "A", "my secret v2");

    assertTrue(NoteCrypto.isEncrypted(disk.load("a").content()));
    assertTrue(repo.isProtectedOnDisk("a"));
    assertEquals(disk.load("a").content(), repo.storedText("a"));
    assertEquals("my secret v2", repo.load("a").content());
    assertEquals(keyring, repo.keyring());
  }

  @Test
  void aLockedNoteIsNeverOverwrittenWithPlainText(@TempDir Path data) {
    FileTabRepository disk = new FileTabRepository(data);
    Keyring keyring = new Keyring();
    EncryptingRepository repo = new EncryptingRepository(disk, keyring);
    repo.protect("a", "A", "my secret", NoteCrypto.newKey(PASSWORD));
    keyring.clear();

    String stored = repo.load("a").content();
    repo.save("a", "A", "leaked plain text");

    assertTrue(NoteCrypto.isEncrypted(stored), "locked: loads encrypted");
    assertEquals(stored, disk.load("a").content());
    NoteCrypto.Key key = NoteCrypto.keyFor(PASSWORD, stored);
    assertEquals("my secret", NoteCrypto.decrypt(stored, key));
  }

  @Test
  void unprotectingStoresPlainTextAgain(@TempDir Path data) {
    FileTabRepository disk = new FileTabRepository(data);
    EncryptingRepository repo = new EncryptingRepository(disk, new Keyring());
    repo.protect("a", "A", "text", NoteCrypto.newKey(PASSWORD));

    repo.unprotect("a", "A", "text");
    repo.save("b", "B", "plain");

    assertEquals("text", disk.load("a").content());
    assertFalse(repo.isProtectedOnDisk("a"));
    assertEquals("plain", repo.load("b").content());
  }

  @Test
  void theKeyringLocksAfterIdleTimeOnlyWhenSomethingIsUnlocked() {
    Keyring keyring = new Keyring();
    Instant now = Instant.now();
    Duration tenMinutes = Duration.ofMinutes(10);
    assertFalse(keyring.dueToLock(tenMinutes, now.plus(Duration.ofHours(1))), "nothing unlocked");

    keyring.put("a", NoteCrypto.newKey(PASSWORD));
    keyring.touch(now);

    assertFalse(keyring.dueToLock(tenMinutes, now.plus(Duration.ofMinutes(9))));
    assertTrue(keyring.dueToLock(tenMinutes, now.plus(tenMinutes)));
    assertFalse(keyring.dueToLock(Duration.ZERO, now.plus(Duration.ofDays(1))), "never");
    assertEquals(1, keyring.ids().size());
    assertTrue(keyring.unlocked("a"));
    keyring.remove("a");
    assertFalse(keyring.unlocked("a"));
  }

  @Test
  void aNoteProtectedInAnEarlierSessionIsGuardedBeforeItIsLoaded(@TempDir Path data) {
    FileTabRepository disk = new FileTabRepository(data);
    new EncryptingRepository(disk, new Keyring())
        .protect("a", "A", "secret", NoteCrypto.newKey(PASSWORD));
    String stored = disk.load("a").content();

    EncryptingRepository restarted = new EncryptingRepository(disk, new Keyring());
    restarted.save("a", "A", "plain");

    assertEquals(stored, disk.load("a").content());
  }
}
