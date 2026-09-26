package util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppLogTest {

  private final Thread.UncaughtExceptionHandler before =
      Thread.getDefaultUncaughtExceptionHandler();

  @AfterEach
  void restore() {
    for (Handler h : Logger.getLogger("").getHandlers()) {
      if (h instanceof java.util.logging.FileHandler) {
        Logger.getLogger("").removeHandler(h);
        h.close();
      }
    }
    Thread.setDefaultUncaughtExceptionHandler(before);
  }

  @Test
  void logsIntoTheDataFolderAndRecordsCrashes(@TempDir Path data) throws Exception {
    AppLog.install(data);
    Logger.getLogger("cutenotes").warning("hello log");
    Thread.getDefaultUncaughtExceptionHandler()
        .uncaughtException(Thread.currentThread(), new IllegalStateException("boom"));
    for (Handler h : Logger.getLogger("").getHandlers()) {
      h.flush();
    }

    Path log = AppLog.folder(data).resolve("cutenotes-0.log");
    String text = Files.readString(log);
    assertTrue(text.contains("hello log"));
    assertTrue(text.contains("boom"));
  }
}
