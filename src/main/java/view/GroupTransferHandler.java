package view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;

import model.TabMeta;

/** Moves notes and groups by dragging them onto a target group (or the bucket to ungroup). */
class GroupTransferHandler extends TransferHandler {

  private static final String NOTE = "note:";
  private static final String GROUP = "group:";

  private final transient GroupTree tree;
  private final transient NoteGroupActions actions;

  GroupTransferHandler(GroupTree tree, NoteGroupActions actions) {
    this.tree = tree;
    this.actions = actions;
  }

  @Override
  public int getSourceActions(JComponent c) {
    return MOVE;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    TabMeta note = tree.selectedNote();
    if (note != null) {
      return new StringSelection(NOTE + note.id());
    }
    String groupId = tree.selectedGroupId();
    return groupId == null ? null : new StringSelection(GROUP + groupId);
  }

  @Override
  public boolean canImport(TransferSupport support) {
    return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
  }

  @Override
  public boolean importData(TransferSupport support) {
    if (!canImport(support)) {
      return false;
    }
    JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
    String target = tree.dropTargetGroupId(location.getPath());
    String token = readToken(support);
    if (token == null) {
      return false;
    }
    if (token.startsWith(NOTE)) {
      actions.moveNote(token.substring(NOTE.length()), target);
      return true;
    }
    if (token.startsWith(GROUP)) {
      actions.moveGroup(token.substring(GROUP.length()), target);
      return true;
    }
    return false;
  }

  private static String readToken(TransferSupport support) {
    try {
      return (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
    } catch (UnsupportedFlavorException | IOException e) {
      return null;
    }
  }
}
