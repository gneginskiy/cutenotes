package view;

import static view.Messages.tr;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import model.TabMeta;
import util.NoteIndex;

/**
 * Top of the notes browser: a search box that filters names and text while typing ({@code #tag}
 * filters by tag; Enter opens the best match, ↓ moves into the list), the sort order, a tag list, a
 * "show open notes" switch and a "new group" button.
 */
final class BrowserToolbar extends JPanel {

  private final SearchField search = new SearchField(tr("browser.search"));
  private final GroupTree tree;

  BrowserToolbar(GroupTree tree, NoteGroupActions actions, Runnable openBest, UiPalette p) {
    super(new BorderLayout(8, 0));
    this.tree = tree;
    Box options = Box.createHorizontalBox();
    options.add(showOpen(p));
    options.add(sortBox());
    options.add(
        button(
            VectorIcon.Kind.SEARCH,
            "#",
            tr("browser.search"),
            () -> TagsPopup.show(tree, search),
            p));
    options.add(
        button(
            VectorIcon.Kind.FOLDER,
            tr("browser.newGroup"),
            tr("browser.newGroup.tip"),
            () -> actions.newGroup(null),
            p));
    options.add(
        button(VectorIcon.Kind.TRASH, null, tr("browser.trash.tip"), actions::showTrash, p));
    search.applyPalette(p);
    add(search, BorderLayout.CENTER);
    add(options, BorderLayout.EAST);
    setBackground(p.chrome());
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, p.border()),
            BorderFactory.createEmptyBorder(10, 10, 10, 8)));
    search.input().getDocument().addDocumentListener(DocumentChanges.on(this::filter));
    search.input().addKeyListener(new BrowserSearchKeys(tree, search.input(), openBest));
  }

  HintField input() {
    return search.input();
  }

  /** Continues typing that started in the tree inside the search box. */
  void typeIntoSearch(char c) {
    search.input().requestFocusInWindow();
    search.input().replaceSelection(String.valueOf(c));
  }

  /** The full-text index is ready: search by text from now on. */
  void useIndex(NoteIndex index) {
    tree.query().change(f -> f.withIndex(index));
    filter();
  }

  /** The note Enter should open: the selected one, else the first match. */
  TabMeta best() {
    TabMeta selected = tree.selectedNote();
    return selected != null ? selected : tree.selectFirstNote();
  }

  private void filter() {
    String query = search.input().getText();
    tree.query().change(f -> f.withQuery(query));
    tree.refresh();
    if (query.isBlank()) {
      search.setCounter("", false);
      return;
    }
    int found = tree.currentNoteOrder().size();
    search.setCounter(
        found == 0 ? tr("browser.noResults") : tr("browser.found", found), found == 0);
  }

  private JCheckBox showOpen(UiPalette p) {
    JCheckBox box = new JCheckBox(tr("browser.showOpen"), true);
    box.setFocusable(false);
    box.setOpaque(false);
    box.setForeground(p.fg());
    box.addActionListener(
        e -> {
          tree.query().change(f -> f.withShowOpen(box.isSelected()));
          tree.refresh();
        });
    return box;
  }

  private JComboBox<BrowserQuery.Sort> sortBox() {
    JComboBox<BrowserQuery.Sort> box = new JComboBox<>(BrowserQuery.Sort.values());
    box.setFocusable(false);
    box.setSelectedItem(tree.query().sort());
    box.addActionListener(
        e -> {
          tree.query().setSort((BrowserQuery.Sort) box.getSelectedItem());
          tree.refresh();
        });
    return box;
  }

  private static FlatButton button(
      VectorIcon.Kind kind, String text, String tip, Runnable action, UiPalette p) {
    FlatButton b = new FlatButton(kind, text, tip, action);
    b.applyPalette(p);
    return b;
  }
}
