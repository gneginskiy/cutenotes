package view;

import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dao.FileOptionsStore;
import dao.OptionsStore;
import model.Theme;

final class AppOptions {

  private final JFrame owner;
  private final OptionsStore store;
  private final Consumer<Theme> onApply;
  private AppSettingsPanel.Hooks hooks;
  private OptionsDialog dialog;

  AppOptions(JFrame owner, Consumer<Theme> onApply) {
    this.owner = owner;
    this.store = new FileOptionsStore();
    this.onApply = onApply;
    SwingUtilities.invokeLater(this::ensureBuilt);
  }

  /** What the Application page sets in motion; set before the dialog is first built. */
  void setHooks(AppSettingsPanel.Hooks hooks) {
    this.hooks = hooks;
  }

  /** Applies the current theme again (after a setting that changes how it lays out). */
  void reapply() {
    onApply.accept(ThemeHolder.current());
  }

  void openDialog() {
    ensureBuilt();
    dialog.openFor(ThemeHolder.current());
  }

  private void ensureBuilt() {
    if (dialog == null) {
      dialog = new OptionsDialog(owner, this::applyTheme, hooks);
    }
  }

  void applyTheme(Theme t) {
    if (t.equals(ThemeHolder.current())) {
      return;
    }
    ThemeHolder.set(t);
    store.save(t);
    onApply.accept(t);
  }
}
