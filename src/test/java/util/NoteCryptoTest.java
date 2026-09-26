package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NoteCryptoTest {

  private static final char[] PASSWORD = "correct horse".toCharArray();

  @Test
  void encryptsAndDecryptsWithTheSamePassword() {
    NoteCrypto.Key key = NoteCrypto.newKey(PASSWORD);
    String plain = "-----BEGIN KEY-----\nсекрет **bold** ![](images/a.png)";

    String stored = NoteCrypto.encrypt(plain, key);

    assertTrue(NoteCrypto.isEncrypted(stored));
    assertFalse(stored.contains("секрет"));
    assertEquals(plain, NoteCrypto.decrypt(stored, NoteCrypto.keyFor(PASSWORD, stored)));
    assertEquals(plain, NoteCrypto.decrypt(stored, key));
  }

  @Test
  void everyEncryptionLooksDifferent() {
    NoteCrypto.Key key = NoteCrypto.newKey(PASSWORD);

    assertNotEquals(NoteCrypto.encrypt("same", key), NoteCrypto.encrypt("same", key));
  }

  @Test
  void wrongPasswordOrDamagedTextIsRejected() {
    String stored = NoteCrypto.encrypt("text", NoteCrypto.newKey(PASSWORD));

    assertThrows(
        IllegalArgumentException.class,
        () -> NoteCrypto.decrypt(stored, NoteCrypto.keyFor("wrong".toCharArray(), stored)));
    String tampered = stored.substring(0, stored.length() - 4) + "AAAA";
    assertThrows(
        IllegalArgumentException.class,
        () -> NoteCrypto.decrypt(tampered, NoteCrypto.keyFor(PASSWORD, tampered)));
    assertThrows(IllegalArgumentException.class, () -> NoteCrypto.keyFor(PASSWORD, "plain text"));
    assertThrows(
        IllegalArgumentException.class,
        () -> NoteCrypto.keyFor(PASSWORD, NoteCrypto.PREFIX + "only:two"));
    assertThrows(
        IllegalArgumentException.class,
        () -> NoteCrypto.keyFor(PASSWORD, NoteCrypto.PREFIX + "!!:??:##"));
    assertFalse(NoteCrypto.isEncrypted(null));
  }
}
