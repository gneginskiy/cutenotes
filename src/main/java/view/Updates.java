package view;

import static view.Messages.tr;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import javax.swing.SwingUtilities;

import util.AppVersion;
import util.UpdateChecker;

/** Welcome and "updated" notes at start, and the daily look for a newer release. */
final class Updates {

  static final Duration CHECK_EVERY = Duration.ofDays(1);

  private Updates() {}

  /** Tells a first-time user where things are, and a returning one that the app was updated. */
  static void greet(Toast toast) {
    String version = AppVersion.current();
    String seen = SettingsHolder.current().lastSeenVersion();
    if (seen.isEmpty()) {
      toast.flash(tr("toast.welcome"));
    } else if (!seen.equals(version) && !AppVersion.isDev()) {
      toast.flash(tr("toast.updated", version));
    }
    if (!seen.equals(version)) {
      SettingsHolder.update(s -> s.withLastSeenVersion(version));
    }
  }

  /** Looks for a new release at most once a day, unless turned off or a development build. */
  static void checkAtStartup(Toast toast) {
    long now = Instant.now().toEpochMilli();
    boolean due = now - SettingsHolder.current().lastUpdateCheck() > CHECK_EVERY.toMillis();
    if (SettingsHolder.current().checkUpdates() && due && !AppVersion.isDev()) {
      SettingsHolder.update(s -> s.withLastUpdateCheck(now));
      check(toast, false);
    }
  }

  /** Asks GitHub in the background; a manual check always reports, an automatic one only news. */
  static void check(Toast toast, boolean manual) {
    Thread.ofVirtual()
        .name("update-check")
        .start(
            () -> {
              String latest = UpdateChecker.latestTag(HttpClient.newHttpClient());
              SwingUtilities.invokeLater(() -> report(toast, latest, manual));
            });
  }

  static void report(Toast toast, String latest, boolean manual) {
    if (latest != null && UpdateChecker.newer(latest, AppVersion.current())) {
      toast.flash(tr("toast.updateAvailable", latest.replaceFirst("^v", "")));
      if (manual) {
        Desktops.browse(UpdateChecker.pageOf(latest));
      }
    } else if (manual) {
      toast.flash(tr(latest == null ? "toast.updateFailed" : "toast.upToDate"));
    }
  }
}
