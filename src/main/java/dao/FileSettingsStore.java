package dao;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import model.AppSettings;

/** Stores {@link AppSettings} as {@code key=value} lines; unknown or broken values fall back. */
public class FileSettingsStore implements SettingsStore {

  private static final String LANGUAGE = "language";
  private static final String FOLLOW_SYSTEM = "followSystemTheme";
  private static final String LIGHT = "lightPreset";
  private static final String DARK = "darkPreset";
  private static final String LINE_WIDTH = "lineWidth";
  private static final String CHECK_UPDATES = "checkUpdates";
  private static final String TRAY = "trayIcon";
  private static final String AUTO_LOCK = "autoLockMinutes";
  private static final String LAST_SEEN = "lastSeenVersion";
  private static final String LAST_CHECK = "lastUpdateCheck";

  private final Path file;

  public FileSettingsStore() {
    this(DataDir.resolve().resolve("cutenotes_settings.txt"));
  }

  public FileSettingsStore(Path file) {
    this.file = file;
  }

  @Override
  public AppSettings load() {
    Map<String, String> m = KeyValueFile.read(file);
    AppSettings d = AppSettings.DEFAULT;
    return new AppSettings(
        m.getOrDefault(LANGUAGE, d.language()),
        bool(m.get(FOLLOW_SYSTEM), d.followSystemTheme()),
        m.getOrDefault(LIGHT, d.lightPreset()),
        m.getOrDefault(DARK, d.darkPreset()),
        Math.max(0, KeyValueFile.intOr(m.get(LINE_WIDTH), d.lineWidth())),
        bool(m.get(CHECK_UPDATES), d.checkUpdates()),
        bool(m.get(TRAY), d.trayIcon()),
        Math.max(0, KeyValueFile.intOr(m.get(AUTO_LOCK), d.autoLockMinutes())),
        m.getOrDefault(LAST_SEEN, d.lastSeenVersion()),
        KeyValueFile.longOr(m.get(LAST_CHECK), d.lastUpdateCheck()));
  }

  @Override
  public void save(AppSettings s) {
    Map<String, String> m = new LinkedHashMap<>();
    m.put(LANGUAGE, s.language());
    m.put(FOLLOW_SYSTEM, String.valueOf(s.followSystemTheme()));
    m.put(LIGHT, s.lightPreset());
    m.put(DARK, s.darkPreset());
    m.put(LINE_WIDTH, String.valueOf(s.lineWidth()));
    m.put(CHECK_UPDATES, String.valueOf(s.checkUpdates()));
    m.put(TRAY, String.valueOf(s.trayIcon()));
    m.put(AUTO_LOCK, String.valueOf(s.autoLockMinutes()));
    m.put(LAST_SEEN, s.lastSeenVersion());
    m.put(LAST_CHECK, String.valueOf(s.lastUpdateCheck()));
    KeyValueFile.write(file, m);
  }

  private static boolean bool(String value, boolean fallback) {
    return value == null ? fallback : Boolean.parseBoolean(value);
  }
}
