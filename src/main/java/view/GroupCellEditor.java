package view;

import java.util.EventObject;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import model.Group;

/** In-place tree editor that only allows renaming group nodes (not notes or the bucket). */
class GroupCellEditor extends DefaultTreeCellEditor {

  GroupCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
    super(tree, renderer);
  }

  @Override
  public boolean isCellEditable(EventObject event) {
    if (!super.isCellEditable(event)) {
      return false;
    }
    TreePath path = tree.getSelectionPath();
    return path != null
        && ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject() instanceof Group;
  }
}
