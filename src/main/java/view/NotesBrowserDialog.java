package view;

import static view.Messages.tr;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import dao.GroupStore;
import dao.TabRepository;
import model.Tab;
import model.TabMeta;
import util.NoteIndex;

/**
 * Browses every note in a group tree: type to filter and Enter to open (a quick switcher), drag to
 * regroup, rename inline, delete.
 */
class NotesBrowserDialog extends JDialog {

  /**
   * What the browser asks the owner to do: open a note (at a text match), delete it (closing its
   * tab if open), show that its settings (colour) changed, or show "Recently deleted".
   */
  record Callbacks(
      Consumer<String> open,
      BiConsumer<String, String> openFound,
      Consumer<String> delete,
      Consumer<String> changed,
      Runnable showTrash) {}

  /** Where the browser reads from. */
  record Source(TabRepository repo, GroupStore groups, NoteMetas metas, Set<String> openIds) {}

  private final GroupTree tree;
  private final transient NoteGroupActions actions;
  private final BrowserToolbar toolbar;

  NotesBrowserDialog(JFrame owner, Source source, Callbacks callbacks) {
    super(owner, tr("browser.title"), false);
    UiPalette palette = UiPalette.current();
    List<TabMeta> notes =
        source.repo().listMeta().stream().filter(m -> !Tab.DEFAULT_ID.equals(m.id())).toList();
    this.tree = new GroupTree(notes, source.openIds(), source.groups().read(), source.metas());
    this.actions =
        new NoteGroupActions(this, tree, source.groups(), callbacks, new Toast(getRootPane()));
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
    setSize(560, 540);
    WindowPlacement.centerOnOwner(this);
    setAlwaysOnTop(true);
    indexInBackground(source.repo());
  }

  static void open(JFrame owner, Source source, Callbacks callbacks) {
    new NotesBrowserDialog(owner, source, callbacks).setVisible(true);
  }

  /** Reading every note takes a moment: names are searchable at once, text when this is done. */
  private void indexInBackground(TabRepository repo) {
    Thread.ofVirtual()
        .name("note-index")
        .start(
            () -> {
              NoteIndex index = NoteIndex.build(repo);
              SwingUtilities.invokeLater(
                  () -> {
                    if (isDisplayable()) {
                      toolbar.useIndex(index);
                    }
                  });
            });
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
