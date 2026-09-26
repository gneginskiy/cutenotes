package view;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/** Opens web pages and folders with the operating system; silently does nothing where it can't. */
final class Desktops {

  private Desktops() {}

  static void browse(String link) {
    if (link == null || !supported(Desktop.Action.BROWSE)) {
      return;
    }
    try {
      Desktop.getDesktop().browse(URI.create(link));
    } catch (Exception e) {
      // not a valid address, or no browser
    }
  }

  static void open(Path folder) {
    if (folder == null || !supported(Desktop.Action.OPEN)) {
      return;
    }
    try {
      Files.createDirectories(folder);
      Desktop.getDesktop().open(folder.toFile());
    } catch (Exception e) {
      // no file manager
    }
  }

  private static boolean supported(Desktop.Action action) {
    return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(action);
  }
}
