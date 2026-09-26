package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Whether the operating system is in dark mode, asked the way each system answers: {@code defaults}
 * on macOS, the registry on Windows, {@code gsettings} on Linux desktops.
 */
public final class SystemDarkMode {

  private static final long TIMEOUT_MS = 2_000;

  private SystemDarkMode() {}

  /** The command that tells, for an {@code os.name}. */
  static List<String> command(String osName) {
    String os = osName.toLowerCase(Locale.ROOT);
    if (os.contains("mac")) {
      return List.of("defaults", "read", "-g", "AppleInterfaceStyle");
    }
    if (os.contains("win")) {
      return List.of(
          "reg",
          "query",
          "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
          "/v",
          "AppsUseLightTheme");
    }
    return List.of("gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
  }

  /** Reads the command's output; empty when it does not say. */
  static Optional<Boolean> parse(String osName, int exitCode, String output) {
    String os = osName.toLowerCase(Locale.ROOT);
    String out = output.toLowerCase(Locale.ROOT);
    if (os.contains("mac")) {
      // without a dark style the key does not exist and defaults fails: light
      return Optional.of(exitCode == 0 && out.contains("dark"));
    }
    if (exitCode != 0) {
      return Optional.empty();
    }
    String dark = os.contains("win") ? "0x0" : "prefer-dark";
    String light = os.contains("win") ? "0x1" : "default";
    if (out.contains(dark)) {
      return Optional.of(true);
    }
    return out.contains(light) || out.contains("prefer-light")
        ? Optional.of(false)
        : Optional.empty();
  }

  /** Asks the system; empty when it cannot tell. */
  public static Optional<Boolean> detect() {
    String os = System.getProperty("os.name", "");
    try {
      Process p = new ProcessBuilder(command(os)).redirectErrorStream(true).start();
      if (!p.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
        p.destroyForcibly();
        return Optional.empty();
      }
      String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      return parse(os, p.exitValue(), out);
    } catch (IOException e) {
      return Optional.empty();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    }
  }
}
