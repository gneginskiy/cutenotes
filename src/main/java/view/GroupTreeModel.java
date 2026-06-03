package view;

import java.util.function.BiConsumer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Tree model that routes in-place edits to a rename callback instead of mutating nodes directly.
 */
class GroupTreeModel extends DefaultTreeModel {

  private final transient BiConsumer<TreePath, Object> onCommit;

  GroupTreeModel(TreeNode root, BiConsumer<TreePath, Object> onCommit) {
    super(root, true);
    this.onCommit = onCommit;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    onCommit.accept(path, newValue);
  }
}
