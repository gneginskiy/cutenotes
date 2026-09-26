package view;

import static view.Messages.tr;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dao.DataDir;
import model.AppSettings;
import model.ThemePreset;
import model.ThemePresets;

/**
 * Options › Application: language, following the system's light / dark mode, line width, updates,
 * tray icon, auto-lock and the notes folder. Every change applies (and is saved) at once.
 */
final class AppSettingsPanel {

  /** What changing a setting sets in motion outside the settings themselves. */
  record Hooks(
      Consumer<String> notifier,
      Runnable themeRule,
      Runnable refit,
      Runnable tray,
      Runnable chooseFolder) {}

  private static final List<String> LANGUAGES = List.of("system", "en", "ru");
  private static final List<Integer> WIDTHS = List.of(0, 60, 72, 80, 100, 120);
  private static final List<Integer> LOCK_MINUTES = List.of(0, 1, 5, 10, 15, 30, 60);

  private AppSettingsPanel() {}

  static JPanel build(Hooks hooks) {
    AppSettings s = SettingsHolder.current();
    Box box = Box.createVerticalBox();
    box.add(OptionsForm.left(OptionsForm.caption(tr("options.app"))));
    box.add(SettingControls.row(tr("options.language"), language(s, hooks)));
    look(box, s, hooks);
    box.add(
        OptionsForm.left(
            SettingControls.check(
                tr("options.checkUpdates"),
                s.checkUpdates(),
                () -> {},
                AppSettings::withCheckUpdates)));
    box.add(
        OptionsForm.left(
            SettingControls.check(
                tr("options.tray"), s.trayIcon(), hooks.tray(), AppSettings::withTrayIcon)));
    box.add(autoLock(s));
    folder(box, hooks);
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 14, 16));
    panel.add(box, BorderLayout.NORTH);
    return panel;
  }

  private static JComponent autoLock(AppSettings s) {
    return SettingControls.row(
        tr("options.autoLock"),
        SettingControls.choice(
            LOCK_MINUTES,
            s.autoLockMinutes(),
            m -> m == 0 ? tr("options.autoLock.never") : tr("options.autoLock.minutes", m),
            m -> SettingControls.apply(x -> x.withAutoLockMinutes(m), () -> {})));
  }

  /** Light / dark presets that follow the system, and the line width. */
  private static void look(Box box, AppSettings s, Hooks hooks) {
    box.add(
        OptionsForm.left(
            SettingControls.check(
                tr("options.followSystem"),
                s.followSystemTheme(),
                hooks.themeRule(),
                AppSettings::withFollowSystemTheme)));
    box.add(
        SettingControls.row(
            tr("options.lightTheme"),
            preset(s.lightPreset(), hooks, AppSettings::withLightPreset)));
    box.add(
        SettingControls.row(
            tr("options.darkTheme"), preset(s.darkPreset(), hooks, AppSettings::withDarkPreset)));
    box.add(
        SettingControls.row(
            tr("options.lineWidth"),
            SettingControls.choice(
                WIDTHS,
                s.lineWidth(),
                w -> w == 0 ? tr("options.lineWidth.full") : tr("options.lineWidth.chars", w),
                w -> SettingControls.apply(x -> x.withLineWidth(w), hooks.refit()))));
  }

  private static void folder(Box box, Hooks hooks) {
    box.add(Box.createVerticalStrut(12));
    box.add(OptionsForm.left(OptionsForm.caption(tr("options.folder"))));
    FlatButton change = new FlatButton(tr("options.folder.change"), null, hooks.chooseFolder());
    change.applyPalette(UiPalette.current());
    box.add(OptionsForm.left(OptionsForm.row(new JLabel(DataDir.resolve() + "  "), change)));
  }

  private static JComboBox<String> language(AppSettings s, Hooks hooks) {
    return SettingControls.choice(
        LANGUAGES,
        s.language(),
        l -> tr("options.language." + l),
        l ->
            SettingControls.apply(
                x -> x.withLanguage(l), () -> hooks.notifier().accept(tr("options.restart"))));
  }

  private static JComboBox<String> preset(
      String current, Hooks hooks, BiFunction<AppSettings, String, AppSettings> with) {
    List<String> names = ThemePresets.ALL.stream().map(ThemePreset::name).toList();
    return SettingControls.choice(
        names,
        current,
        n -> n,
        n -> SettingControls.apply(x -> with.apply(x, n), hooks.themeRule()));
  }
}
