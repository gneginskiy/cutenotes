package dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InstanceLockTest {

  @Test
  void secondInstanceIsRejectedWhileTheFirstHoldsTheLock(@TempDir Path tmp) {
    try (InstanceLock first = InstanceLock.tryAcquire(tmp)) {
      assertNotNull(first);
      assertNull(InstanceLock.tryAcquire(tmp));
    }
  }

  @Test
  void lockCanBeTakenAgainAfterRelease(@TempDir Path tmp) {
    InstanceLock first = InstanceLock.tryAcquire(tmp);
    first.close();

    try (InstanceLock again = InstanceLock.tryAcquire(tmp)) {
      assertNotNull(again);
    }
  }

  @Test
  void unlockableFolderDoesNotBlockStartup(@TempDir Path tmp) {
    try (InstanceLock lock = InstanceLock.tryAcquire(tmp.resolve("missing/dir"))) {
      assertNotNull(lock);
    }
  }
}
