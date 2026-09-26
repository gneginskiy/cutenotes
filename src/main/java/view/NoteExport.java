package view;

import static view.Messages.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Theme;
import util.ExportFormat;

/** Exporting a note to a file and printing it. */
final class NoteExport {

  /** Printed text width in points (A4 / Letter minus margins). */
  private static final int PRINT_WIDTH = 468;

  private static final int PRINT_FONT_SIZE = 11;

  private NoteExport() {}

  static void export(Component parent, String title, String md, Toast toast) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(tr("export.title", title));
    chooser.setAcceptAllFileFilterUsed(false);
    for (ExportFormat f : ExportFormat.values()) {
      chooser.addChoosableFileFilter(filter(f));
    }
    chooser.setFileFilter(chooser.getChoosableFileFilters()[0]);
    chooser.setSelectedFile(new File(ExportFormat.safeName(title) + ".md"));
    if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    ExportFormat format = formatOf(chooser.getFileFilter());
    File chosen = chooser.getSelectedFile();
    format = ExportFormat.ofFile(chosen.getName(), format);
    Path file = chosen.toPath().resolveSibling(format.withExtension(chosen.getName()));
    try {
      Files.writeString(
          file, format.render(title, md, NoteExport::imageUrl), StandardCharsets.UTF_8);
      toast.flash(tr("export.done", file.getFileName()));
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          parent, tr("export.failed", e.getMessage()), title, JOptionPane.ERROR_MESSAGE);
    }
  }

  /** Prints on white paper whatever the window theme, in the note's font. */
  static void print(Component parent, String md, Theme current) {
    NoteEditor copy = new NoteEditor(md);
    copy.applyTheme(
        new Theme(
            Color.WHITE,
            Color.BLACK,
            null,
            current.codeColor(),
            current.fontFamily(),
            PRINT_FONT_SIZE,
            null,
            false));
    copy.setSize(new Dimension(PRINT_WIDTH, Short.MAX_VALUE));
    try {
      copy.print(null, null, true, null, null, true);
    } catch (PrinterException e) {
      JOptionPane.showMessageDialog(
          parent, tr("print.failed", e.getMessage()), null, JOptionPane.ERROR_MESSAGE);
    }
  }

  private static String imageUrl(String path) {
    Path file = NoteImages.store().resolve(path);
    return file == null ? path : file.toUri().toString();
  }

  private static FileFilter filter(ExportFormat f) {
    return new FileNameExtensionFilter(tr("export." + f.extension()), f.extension());
  }

  private static ExportFormat formatOf(FileFilter filter) {
    for (ExportFormat f : ExportFormat.values()) {
      if (filter instanceof FileNameExtensionFilter ext
          && ext.getExtensions()[0].equals(f.extension())) {
        return f;
      }
    }
    return ExportFormat.MARKDOWN;
  }
}
