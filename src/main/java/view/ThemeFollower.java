package view;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

import model.AppSettings;
import model.Theme;
import model.ThemePresets;
import util.SystemDarkMode;

/**
 * Switches between the chosen light and dark presets when the system's appearance changes (Options
 * › Application). The system is asked on a virtual thread every few seconds.
 */
final class ThemeFollower {

  static final Duration EVERY = Duration.ofSeconds(5);

  private final Consumer<Theme> applyTheme;

  ThemeFollower(Consumer<Theme> applyTheme) {
    this.applyTheme = applyTheme;
  }

  void start() {
    Thread.ofVirtual()
        .name("theme-follower")
        .start(
            () -> {
              while (!Thread.currentThread().isInterrupted()) {
                check();
                try {
                  Thread.sleep(EVERY);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }
            });
  }

  /** Asks the system now (off the EDT) and applies the matching preset (on the EDT). */
  void check() {
    if (!SettingsHolder.current().followSystemTheme()) {
      return;
    }
    Optional<Boolean> dark = SystemDarkMode.detect();
    dark.ifPresent(d -> SwingUtilities.invokeLater(() -> apply(d)));
  }

  /** Applies the light or dark preset over the current theme, keeping size and window settings. */
  void apply(boolean dark) {
    AppSettings s = SettingsHolder.current();
    if (!s.followSystemTheme()) {
      return;
    }
    String preset = dark ? s.darkPreset() : s.lightPreset();
    Theme current = ThemeHolder.current();
    Theme wanted = ThemePresets.named(preset).applyTo(current, FontFamilies.installed());
    if (!wanted.equals(current)) {
      applyTheme.accept(wanted);
    }
  }

  /** The rule changed (switched on, another preset): apply it without waiting. */
  void ruleChanged() {
    Thread.ofVirtual().start(this::check);
  }
}
