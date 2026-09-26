package model;

import lombok.With;

/**
 * Application preferences other than the look of the notes ({@link Theme}).
 *
 * @param language {@code system}, {@code en} or {@code ru}
 * @param followSystemTheme switch between {@code lightPreset} and {@code darkPreset} with the OS
 * @param lineWidth maximum line length in characters; 0 means the full window width
 * @param checkUpdates look for a newer release once a day
 * @param trayIcon show an icon in the system tray / menu bar
 * @param autoLockMinutes idle minutes after which password-protected notes lock; 0 means never
 * @param lastSeenVersion the version whose "what's new" the user has already been told about
 * @param lastUpdateCheck epoch millis of the last update check
 */
@With
public record AppSettings(
    String language,
    boolean followSystemTheme,
    String lightPreset,
    String darkPreset,
    int lineWidth,
    boolean checkUpdates,
    boolean trayIcon,
    int autoLockMinutes,
    String lastSeenVersion,
    long lastUpdateCheck) {

  public static final AppSettings DEFAULT =
      new AppSettings("system", false, "Paper", "Graphite", 0, true, true, 10, "", 0);
}
