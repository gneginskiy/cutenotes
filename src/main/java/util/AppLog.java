package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Writes the app's log to {@code <data>/logs} (three rotating files of 1 MB) and records crashes of
 * any thread, so a problem a user reports can actually be investigated.
 */
public final class AppLog {

  static final String DIR = "logs";
  private static final Logger ROOT = Logger.getLogger("");

  private AppLog() {}

  public static Path folder(Path dataDir) {
    return dataDir.resolve(DIR);
  }

  /** Starts logging into the data folder; logging problems never stop the app. */
  public static void install(Path dataDir) {
    System.setProperty(
        "java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %3$s: %5$s%6$s%n");
    try {
      Path dir = folder(dataDir);
      Files.createDirectories(dir);
      FileHandler file =
          new FileHandler(dir.resolve("cutenotes-%g.log").toString(), 1_000_000, 3, true);
      file.setFormatter(new SimpleFormatter());
      ROOT.addHandler(file);
    } catch (IOException | SecurityException e) {
      ROOT.log(Level.WARNING, "no log file", e);
    }
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, error) ->
            Logger.getLogger("cutenotes")
                .log(Level.SEVERE, "uncaught in " + thread.getName(), error));
    Logger.getLogger("cutenotes").info("cuteNotes " + AppVersion.current() + " started");
  }
}
