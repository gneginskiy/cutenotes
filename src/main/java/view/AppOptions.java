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
  private OptionsDialog dialog;

  AppOptions(JFrame owner, Consumer<Theme> onApply) {
    this.owner = owner;
    this.store = new FileOptionsStore();
    this.onApply = onApply;
    SwingUtilities.invokeLater(this::ensureBuilt);
  }

  void openDialog() {
    ensureBuilt();
    dialog.openFor(ThemeHolder.current());
  }

  private void ensureBuilt() {
    if (dialog == null) {
      dialog = new OptionsDialog(owner, this::applyTheme);
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
