package view;

import static view.Messages.tr;

import java.awt.BorderLayout;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.text.DefaultHighlighter;

import model.Theme;

/** Find (and replace) in the current note: a search row and an optional replace row. */
class SearchBar extends JPanel {

  private final SearchField field = new SearchField(tr("search.find"));
  private final HintField input = field.input();
  private final Supplier<NoteEditor> editor;
  private final FindModel model;
  private final SearchControls controls =
      new SearchControls(() -> step(-1), () -> step(1), this::runSearch, this::close);
  private final ReplaceRow replace = new ReplaceRow(this::replaceOne, this::replaceAll);

  SearchBar(Supplier<NoteEditor> editor) {
    super(new BorderLayout());
    this.editor = editor;
    this.model = new FindModel(editor);
    JPanel findRow = new JPanel(new BorderLayout());
    findRow.setOpaque(false);
    findRow.add(field, BorderLayout.CENTER);
    findRow.add(controls, BorderLayout.EAST);
    add(findRow, BorderLayout.NORTH);
    add(replace, BorderLayout.SOUTH);
    setVisible(false);
    applyTheme(ThemeHolder.current());
    input.getDocument().addDocumentListener(DocumentChanges.on(this::runSearch));
    input.addKeyListener(new SearchKeys(this, false));
    replace.input().addKeyListener(new SearchKeys(this, true));
  }

  void applyTheme(Theme t) {
    UiPalette p = UiPalette.of(t);
    setBackground(p.chrome());
    setOpaque(true);
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, p.border()),
            BorderFactory.createEmptyBorder(7, 10, 7, 8)));
    field.applyPalette(p);
    controls.applyPalette(p);
    replace.applyPalette(p);
    model.setPainters(
        new DefaultHighlighter.DefaultHighlightPainter(p.match()),
        new DefaultHighlighter.DefaultHighlightPainter(p.activeMatch()));
  }

  void open() {
    reveal(false);
  }

  /** Opens with {@code query} already searched, e.g. after opening a note found by its text. */
  void openWith(String query) {
    reveal(false);
    input.setText(query);
    input.selectAll();
  }

  /** Opens with the replace row. */
  void openReplace() {
    reveal(true);
  }

  private void reveal(boolean withReplace) {
    replace.setVisible(withReplace);
    setVisible(true);
    revalidate();
    input.requestFocusInWindow();
    input.selectAll();
    runSearch();
  }

  void close() {
    model.clear();
    setVisible(false);
    revalidate();
    NoteEditor area = editor.get();
    if (area != null) {
      area.requestFocusInWindow();
    }
  }

  private void runSearch() {
    NoteEditor area = editor.get();
    model.run(input.getText(), controls.options(), area == null ? 0 : area.getSelectionStart());
    updateCounter();
  }

  void step(int direction) {
    model.step(direction);
    updateCounter();
  }

  void replaceOne() {
    model.replaceCurrent(replace.input().getText());
    updateCounter();
  }

  void replaceAll() {
    int count = model.replaceAll(replace.input().getText());
    updateCounter();
    if (count > 0) {
      field.setCounter(tr("toast.replaced", count), false);
    }
  }

  private void updateCounter() {
    if (model.invalid()) {
      field.setCounter(tr("search.invalid"), true);
    } else if (model.count() == 0) {
      boolean typed = !input.getText().isEmpty();
      field.setCounter(typed ? tr("search.noResults") : "", typed);
    } else {
      field.setCounter(tr("search.counter", model.activeIndex() + 1, model.count()), false);
    }
  }
}
