package view;

import static view.Messages.tr;

import java.awt.Component;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import util.SaveListener;

/**
 * Makes auto-save failures visible: the window title gets a warning prefix and the first failure of
 * an episode raises an alert. Before, every I/O error (disk full, read-only folder, bad file name)
 * was swallowed and the user kept typing into notes that were never written.
 */
final class SaveStatus implements SaveListener {

  static String warning() {
    return tr("save.warning");
  }

  private final Consumer<String> titleSink;
  private final Consumer<Exception> alert;
  private String title;
  private boolean failing;

  SaveStatus(Consumer<String> titleSink, Consumer<Exception> alert, String title) {
    this.titleSink = titleSink;
    this.alert = alert;
    this.title = title;
  }

  static Consumer<Exception> dialogOver(Component parent) {
    return failure ->
        JOptionPane.showMessageDialog(
            parent.isShowing() ? parent : null,
            tr("save.failed", failure.getMessage()),
            tr("save.failedTitle"),
            JOptionPane.WARNING_MESSAGE);
  }

  static BooleanSupplier askQuitUnsaved(Component parent) {
    return () ->
        JOptionPane.showConfirmDialog(
                parent,
                tr("save.quitUnsaved"),
                tr("save.failedTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE)
            == JOptionPane.YES_OPTION;
  }

  void setTitle(String base) {
    this.title = base;
    render();
  }

  @Override
  public void onSaveStatus(Exception failure) {
    SwingUtilities.invokeLater(() -> update(failure));
  }

  void update(Exception failure) {
    boolean wasFailing = failing;
    failing = failure != null;
    render();
    if (failing && !wasFailing) {
      alert.accept(failure);
    }
  }

  private void render() {
    titleSink.accept(failing ? warning() + title : title);
  }
}
