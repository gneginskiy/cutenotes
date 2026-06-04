package view;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import dao.GroupStore;
import dao.TabRepository;
import model.TabMeta;

/** Browses every note in a group tree: reopen, drag to regroup, rename inline, delete. */
class NotesBrowserDialog extends JDialog {

  private final GroupTree tree;
  private final transient NoteGroupActions actions;

  NotesBrowserDialog(
      JFrame owner,
      TabRepository repo,
      GroupStore groups,
      Set<String> openIds,
      Consumer<String> onPick) {
    super(owner, "Notes", false);
    List<TabMeta> notes =
        repo.listMeta().stream().filter(m -> !TabsPane.DEFAULT_ID.equals(m.id())).toList();
    this.tree = new GroupTree(notes, openIds, groups.read());
    this.actions = new NoteGroupActions(this, tree, groups, repo, onPick);
    tree.setOnRename(actions::rename);
    TreeReorder reorder = new TreeReorder(tree, actions);
    tree.addMouseListener(reorder);
    tree.addMouseMotionListener(reorder);
    wireTree();
    add(toolbar(), BorderLayout.NORTH);
    add(new JScrollPane(tree), BorderLayout.CENTER);
    bindClose();
    setSize(380, 460);
    WindowPlacement.centerOnOwner(this);
    setAlwaysOnTop(true);
  }

  private JPanel toolbar() {
    JPanel bar = new JPanel();
    JCheckBox showAll = new JCheckBox("All notes", true);
    showAll.setFocusable(false);
    showAll.addActionListener(e -> tree.setShowAll(showAll.isSelected()));
    JButton newGroup = new JButton("New group");
    newGroup.setFocusable(false);
    newGroup.addActionListener(e -> actions.newGroup(null));
    bar.add(showAll);
    bar.add(newGroup);
    return bar;
  }

  private void wireTree() {
    tree.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            int row = tree.getRowForLocation(e.getX(), e.getY());
            if (row >= 0) {
              tree.setSelectionRow(row);
            }
            maybePopup(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybePopup(e);
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && tree.selectedNote() != null) {
              actions.open(tree.selectedNote().id());
            }
          }
        });
    tree.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            handleKey(e);
          }
        });
  }

  private void maybePopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      NotesContextMenu.show(tree, actions, e.getX(), e.getY());
    }
  }

  private void handleKey(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_ENTER && tree.selectedNote() != null) {
      actions.open(tree.selectedNote().id());
    } else if (code == KeyEvent.VK_F2 && tree.selectedGroupId() != null) {
      tree.editGroup(tree.selectedGroupId());
    } else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE) {
      deleteSelected();
    }
  }

  private void deleteSelected() {
    TabMeta note = tree.selectedNote();
    if (note != null) {
      actions.deleteNote(note);
    } else if (tree.selectedGroupId() != null) {
      actions.deleteGroup(tree.selectedGroupId());
    }
  }

  private void bindClose() {
    int mask = ShortcutMask.menu();
    KeyBindings.bind(
        getRootPane(), KeyStroke.getKeyStroke(KeyEvent.VK_W, mask), "close", this::dispose);
    KeyBindings.bind(
        getRootPane(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close", this::dispose);
  }
}
