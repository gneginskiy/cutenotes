package util;

import java.io.InputStream;
import java.util.Properties;

/** The release this build belongs to ({@code v2026-09-26-3}), or {@code dev} for local builds. */
public final class AppVersion {

  public static final String DEV = "dev";
  private static final String CURRENT = read("/cutenotes/version.properties");

  private AppVersion() {}

  public static String current() {
    return CURRENT;
  }

  public static boolean isDev() {
    return DEV.equals(CURRENT);
  }

  static String read(String resource) {
    try (InputStream in = AppVersion.class.getResourceAsStream(resource)) {
      if (in == null) {
        return DEV;
      }
      Properties p = new Properties();
      p.load(in);
      return clean(p.getProperty("version"));
    } catch (Exception e) {
      return DEV;
    }
  }

  static String clean(String value) {
    return value == null || value.isBlank() || value.contains("${") ? DEV : value.trim();
  }
}
