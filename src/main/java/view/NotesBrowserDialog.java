package view;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import dao.GroupStore;
import dao.TabRepository;
import model.Tab;
import model.TabMeta;

/**
 * Browses every note in a group tree: type to filter and Enter to open (a quick switcher), drag to
 * regroup, rename inline, delete.
 */
class NotesBrowserDialog extends JDialog {

  /** What the browser asks the owner to do: open a note, or delete it (closing its tab if open). */
  record Callbacks(Consumer<String> open, Consumer<String> delete) {}

  private final GroupTree tree;
  private final transient NoteGroupActions actions;
  private final BrowserToolbar toolbar;

  NotesBrowserDialog(
      JFrame owner,
      TabRepository repo,
      GroupStore groups,
      Set<String> openIds,
      Callbacks callbacks) {
    super(owner, "All notes", false);
    UiPalette palette = UiPalette.current();
    List<TabMeta> notes =
        repo.listMeta().stream().filter(m -> !Tab.DEFAULT_ID.equals(m.id())).toList();
    this.tree = new GroupTree(notes, openIds, groups.read());
    this.actions = new NoteGroupActions(this, tree, groups, callbacks);
    this.toolbar = new BrowserToolbar(tree, actions, this::openBestAndClose, palette);
    tree.setOnRename(actions::rename);
    tree.setBackground(palette.bg());
    tree.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    TreeReorder reorder = new TreeReorder(tree, actions);
    tree.addMouseListener(reorder);
    tree.addMouseMotionListener(reorder);
    wireTree();
    getContentPane().setBackground(palette.bg());
    add(toolbar, BorderLayout.NORTH);
    add(TabPanes.content(tree), BorderLayout.CENTER);
    bindClose();
    setSize(440, 520);
    WindowPlacement.centerOnOwner(this);
    setAlwaysOnTop(true);
  }

  static void open(
      JFrame owner, TabRepository repo, GroupStore groups, List<String> openIds, NoteSession s) {
    new NotesBrowserDialog(
            owner, repo, groups, new HashSet<>(openIds), new Callbacks(s::openExisting, s::delete))
        .setVisible(true);
  }

  private void openBestAndClose() {
    TabMeta best = toolbar.best();
    if (best != null) {
      actions.open(best.id());
      dispose();
    }
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
    tree.addKeyListener(new BrowserTreeKeys(tree, actions, toolbar));
  }

  private void maybePopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      NotesContextMenu.show(tree, actions, e.getX(), e.getY());
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
