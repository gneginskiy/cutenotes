package view;

import static view.Messages.tr;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import util.DroppedFile;

/**
 * Files dropped on an editor: text files open as new notes, pictures are inserted where they are
 * dropped. Everything else (dragging text around, the clipboard) is left to the editor's own
 * handler.
 */
final class FileDrop extends TransferHandler {

  private static Consumer<File> opener = f -> {};
  private static Consumer<String> notifier = m -> {};

  private final TransferHandler text;
  private final NoteEditor pane;
  private transient Position dragStart;
  private transient Position dragEnd;

  private FileDrop(NoteEditor pane) {
    this.text = pane.getTransferHandler();
    this.pane = pane;
  }

  static void install(NoteEditor pane) {
    pane.setTransferHandler(new FileDrop(pane));
  }

  /** Where dropped text files go (a new tab) and how problems are shown. */
  static void use(Consumer<File> openAsNote, Consumer<String> notify) {
    opener = openAsNote;
    notifier = notify;
  }

  @Override
  public boolean canImport(TransferSupport support) {
    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || text.canImport(support);
  }

  @Override
  public boolean importData(TransferSupport support) {
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      return importFiles(support);
    }
    if (support.isDrop() && insideDraggedText(support)) {
      return false;
    }
    return text.importData(support);
  }

  @Override
  public int getSourceActions(JComponent c) {
    return text.getSourceActions(c);
  }

  @Override
  public void exportToClipboard(JComponent c, Clipboard clip, int action) {
    text.exportToClipboard(c, clip, action);
  }

  /** Dragging selected text: remembered so a move can remove it where it came from. */
  @Override
  protected Transferable createTransferable(JComponent c) {
    int start = pane.getSelectionStart();
    int end = pane.getSelectionEnd();
    if (start == end) {
      return null;
    }
    try {
      dragStart = pane.getDocument().createPosition(start);
      dragEnd = pane.getDocument().createPosition(end);
    } catch (BadLocationException e) {
      return null;
    }
    return new StringSelection(pane.getSelectedText());
  }

  @Override
  protected void exportDone(JComponent source, Transferable data, int action) {
    if (action == MOVE && dragStart != null) {
      int start = dragStart.getOffset();
      pane.editAsOneStep(doc -> doc.remove(start, dragEnd.getOffset() - start));
    }
    dragStart = null;
    dragEnd = null;
  }

  private boolean insideDraggedText(TransferSupport support) {
    if (dragStart == null
        || !(support.getDropLocation() instanceof JTextComponent.DropLocation d)) {
      return false;
    }
    return d.getIndex() >= dragStart.getOffset() && d.getIndex() <= dragEnd.getOffset();
  }

  @SuppressWarnings("unchecked")
  private boolean importFiles(TransferSupport support) {
    List<File> files;
    try {
      files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
    } catch (Exception e) {
      return false;
    }
    if (support.isDrop() && support.getDropLocation() instanceof JTextComponent.DropLocation d) {
      pane.setCaretPosition(d.getIndex());
    }
    for (File file : files) {
      drop(file);
    }
    return true;
  }

  private void drop(File file) {
    switch (DroppedFile.of(file.getName())) {
      case NOTE -> opener.accept(file);
      case IMAGE -> {
        try {
          BufferedImage image = ImageIO.read(file);
          if (image != null) {
            NoteImages.insert(pane, image);
          }
        } catch (IOException e) {
          notifier.accept(tr("drop.failed", file.getName()));
        }
      }
      case UNSUPPORTED -> notifier.accept(tr("drop.unsupported", file.getName()));
    }
  }
}
