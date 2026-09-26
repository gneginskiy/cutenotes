package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FolderLeaseTest {

  private static final Instant NOW = Instant.parse("2026-09-26T10:00:00Z");

  @Test
  void anotherComputersFreshLeaseIsReported(@TempDir Path data) {
    new FolderLease(data, "laptop").renew(NOW);

    FolderLease.Holder holder = new FolderLease(data, "desktop").otherHolder(NOW.plusSeconds(30));

    assertEquals("laptop", holder.host());
    assertEquals(NOW, holder.since());
  }

  @Test
  void ownStaleMissingOrBrokenLeasesAreNot(@TempDir Path data) throws Exception {
    FolderLease mine = new FolderLease(data, "laptop");
    assertNull(mine.otherHolder(NOW), "no lease yet");

    mine.renew(NOW);
    assertNull(mine.otherHolder(NOW), "my own lease");
    assertNull(new FolderLease(data, "desktop").otherHolder(NOW.plus(Duration.ofMinutes(10))));

    Files.writeString(data.resolve(FolderLease.FILE), "garbage");
    assertNull(new FolderLease(data, "desktop").otherHolder(NOW));
    new FolderLease(data.resolve("missing/deeper"), "x").renew(NOW);
  }

  @Test
  void keepsRenewingInTheBackground(@TempDir Path data) throws Exception {
    new FolderLease(data, FolderLease.localHost()).keepRenewing();

    for (int i = 0; i < 200 && !Files.exists(data.resolve(FolderLease.FILE)); i++) {
      Thread.sleep(10);
    }
    FolderLease.Holder holder = new FolderLease(data, "other").otherHolder(Instant.now());
    assertEquals(FolderLease.localHost(), holder.host());
  }
}
