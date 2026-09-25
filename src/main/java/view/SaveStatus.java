package view;

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

  static final String WARNING = "⚠ NOT SAVED — ";

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
            "Notes cannot be saved: "
                + failure.getMessage()
                + "\nYour text is still in the window. Saving resumes automatically"
                + " once the problem is fixed.",
            "Saving failed",
            JOptionPane.WARNING_MESSAGE);
  }

  static BooleanSupplier askQuitUnsaved(Component parent) {
    return () ->
        JOptionPane.showConfirmDialog(
                parent,
                "Some notes could not be saved and will be lost.\nQuit anyway?",
                "Saving failed",
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
    titleSink.accept(failing ? WARNING + title : title);
  }
}
