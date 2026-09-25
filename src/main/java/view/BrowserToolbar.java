package view;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import model.TabMeta;

/**
 * Top of the notes browser: a search box that filters the tree while typing (Enter opens the first
 * match, ↓ moves into the list), a "show open notes" switch and a "new group" button.
 */
final class BrowserToolbar extends JPanel {

  private final SearchField search = new SearchField("Search notes");
  private final GroupTree tree;
  private final Runnable openBest;

  BrowserToolbar(GroupTree tree, NoteGroupActions actions, Runnable openBest, UiPalette p) {
    super(new BorderLayout(8, 0));
    this.tree = tree;
    this.openBest = openBest;
    JCheckBox showOpen = new JCheckBox("Show open", true);
    showOpen.setFocusable(false);
    showOpen.setOpaque(false);
    showOpen.setForeground(p.fg());
    showOpen.addActionListener(e -> tree.setShowAll(showOpen.isSelected()));
    FlatButton newGroup =
        new FlatButton(
            VectorIcon.Kind.FOLDER, "New group", "Create a group", () -> actions.newGroup(null));
    newGroup.applyPalette(p);
    search.applyPalette(p);
    Box options = Box.createHorizontalBox();
    options.add(showOpen);
    options.add(newGroup);
    add(search, BorderLayout.CENTER);
    add(options, BorderLayout.EAST);
    setBackground(p.chrome());
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, p.border()),
            BorderFactory.createEmptyBorder(10, 10, 10, 8)));
    search.input().getDocument().addDocumentListener(DocumentChanges.on(this::filter));
    search.input().addKeyListener(new SearchKeys());
  }

  HintField input() {
    return search.input();
  }

  /** Continues typing that started in the tree inside the search box. */
  void typeIntoSearch(char c) {
    search.input().requestFocusInWindow();
    search.input().replaceSelection(String.valueOf(c));
  }

  private void filter() {
    String query = search.input().getText();
    tree.setQuery(query);
    if (query.isBlank()) {
      search.setCounter("", false);
      return;
    }
    int found = tree.currentNoteOrder().size();
    search.setCounter(
        found == 0 ? "No results" : found + (found == 1 ? " note" : " notes"), found == 0);
  }

  private final class SearchKeys extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_ENTER -> openBest.run();
        case KeyEvent.VK_DOWN -> {
          if (tree.selectedNote() == null) {
            tree.selectFirstNote();
          }
          tree.requestFocusInWindow();
        }
        case KeyEvent.VK_ESCAPE -> {
          if (!search.input().getText().isEmpty()) {
            search.input().setText("");
            e.consume();
          }
        }
        default -> {
          // other keys edit the query
        }
      }
    }
  }

  /** The note Enter should open: the selected one, else the first match. */
  TabMeta best() {
    TabMeta selected = tree.selectedNote();
    return selected != null ? selected : tree.selectFirstNote();
  }
}
