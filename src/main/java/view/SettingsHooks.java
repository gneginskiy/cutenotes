package view;

/** Connects Options › Application to the running window: theme rule, layout, tray, folder. */
final class SettingsHooks {

  private SettingsHooks() {}

  static void install(AppContext c, AppTray tray) {
    ThemeFollower follower = new ThemeFollower(c.options()::applyTheme);
    c.options()
        .setHooks(
            new AppSettingsPanel.Hooks(
                c.toast()::flash,
                follower::ruleChanged,
                c.options()::reapply,
                tray::update,
                () -> FolderChooser.choose(c.frame(), c.toast()::flash)));
    follower.start();
    tray.update();
  }
}
