package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.AppSettings;
import util.AppVersion;

class UpdatesTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @AfterEach
  void reset() {
    SettingsHolder.set(AppSettings.DEFAULT);
  }

  @Test
  void firstStartWelcomesAndRemembersTheVersion() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          SettingsHolder.set(AppSettings.DEFAULT);
          Toast toast = new Toast(new JRootPane());

          Updates.greet(toast);

          assertEquals(Messages.tr("toast.welcome"), toast.text());
          assertEquals(AppVersion.current(), SettingsHolder.current().lastSeenVersion());
        });
  }

  @Test
  void aManualCheckAlwaysAnswers() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          Toast toast = new Toast(new JRootPane());

          Updates.report(toast, null, true);
          assertEquals(Messages.tr("toast.updateFailed"), toast.text());

          Updates.report(toast, "v2000-01-01", true);
          assertEquals(Messages.tr("toast.upToDate"), toast.text());
        });
  }
}
