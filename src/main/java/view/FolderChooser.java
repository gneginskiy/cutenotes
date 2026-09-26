package view;

import static view.Messages.tr;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import dao.DataDir;
import dao.DataFiles;
import dao.DataLocation;

/**
 * Options › Notes folder › Change…: pick another folder (e.g. inside Dropbox or iCloud Drive),
 * optionally copy the current notes there, and use it from the next start.
 */
final class FolderChooser {

  private FolderChooser() {}

  static void choose(Component parent, Consumer<String> notifier) {
    Path current = DataDir.resolve();
    Path chosen = pick(parent, current);
    if (chosen == null) {
      return;
    }
    int copy =
        JOptionPane.showConfirmDialog(
            parent,
            tr("options.folder.copy"),
            tr("options.folder"),
            JOptionPane.YES_NO_CANCEL_OPTION);
    if (copy == JOptionPane.CANCEL_OPTION || copy == JOptionPane.CLOSED_OPTION) {
      return;
    }
    try {
      if (copy == JOptionPane.YES_OPTION) {
        DataFiles.copy(current, chosen);
      }
      DataLocation.write(DataLocation.home(), chosen);
      JOptionPane.showMessageDialog(parent, tr("options.folder.restart"));
      notifier.accept(tr("options.folder.moved"));
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          parent, e.getMessage(), tr("options.folder"), JOptionPane.ERROR_MESSAGE);
    }
  }

  /** Another folder, or {@code null} when cancelled or the same one was picked. */
  private static Path pick(Component parent, Path current) {
    JFileChooser chooser = new JFileChooser(current.toFile());
    chooser.setDialogTitle(tr("options.folder.chooseTitle"));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    Path chosen = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
    return chosen.equals(current.toAbsolutePath().normalize()) ? null : chosen;
  }
}
