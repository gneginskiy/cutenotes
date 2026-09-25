package view;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import model.Theme;

class SearchBar extends JPanel {

  private final SearchField field = new SearchField("Find in note");
  private final HintField input = field.input();
  private final SearchControls controls =
      new SearchControls(() -> step(-1), () -> step(1), this::runSearch, this::close);
  private final Supplier<JTextComponent> areaSupplier;
  private final List<int[]> matches = new ArrayList<>();
  private Highlighter.HighlightPainter matchPainter;
  private Highlighter.HighlightPainter activePainter;
  private int activeIdx = -1;

  SearchBar(Supplier<JTextComponent> areaSupplier) {
    super(new BorderLayout());
    this.areaSupplier = areaSupplier;
    add(field, BorderLayout.CENTER);
    add(controls, BorderLayout.EAST);
    setVisible(false);
    applyTheme(ThemeHolder.current());
    wireInput();
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
    matchPainter = new DefaultHighlighter.DefaultHighlightPainter(p.match());
    activePainter = new DefaultHighlighter.DefaultHighlightPainter(p.activeMatch());
  }

  private void wireInput() {
    input.getDocument().addDocumentListener(DocumentChanges.on(this::runSearch));
    input.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            handleKey(e);
          }
        });
  }

  void open() {
    setVisible(true);
    revalidate();
    input.requestFocusInWindow();
    input.selectAll();
    runSearch();
  }

  void close() {
    SearchEngine.clear(areaSupplier.get());
    matches.clear();
    activeIdx = -1;
    setVisible(false);
    revalidate();
    JTextComponent area = areaSupplier.get();
    if (area != null) {
      area.requestFocusInWindow();
    }
  }

  private void handleKey(KeyEvent e) {
    int c = e.getKeyCode();
    if (c == KeyEvent.VK_ESCAPE) {
      close();
      e.consume();
    } else if (c == KeyEvent.VK_ENTER) {
      step(e.isShiftDown() ? -1 : 1);
    } else if (c == KeyEvent.VK_DOWN) {
      step(1);
    } else if (c == KeyEvent.VK_UP) {
      step(-1);
    }
  }

  private void runSearch() {
    JTextComponent area = areaSupplier.get();
    matches.clear();
    matches.addAll(
        SearchEngine.findAll(
            area == null ? "" : area.getText(), input.getText(), controls.isCaseSensitive()));
    activeIdx = matches.isEmpty() ? -1 : 0;
    SearchEngine.highlight(area, matches, activeIdx, matchPainter, activePainter);
    updateCounter();
    scrollToActive();
  }

  private void step(int direction) {
    if (matches.isEmpty()) {
      return;
    }
    activeIdx = SearchEngine.step(activeIdx, matches.size(), direction);
    SearchEngine.highlight(areaSupplier.get(), matches, activeIdx, matchPainter, activePainter);
    updateCounter();
    scrollToActive();
  }

  private void updateCounter() {
    if (matches.isEmpty()) {
      boolean typed = !input.getText().isEmpty();
      field.setCounter(typed ? "No results" : "", typed);
    } else {
      field.setCounter((activeIdx + 1) + " of " + matches.size(), false);
    }
  }

  private void scrollToActive() {
    if (activeIdx >= 0) {
      SearchEngine.scrollTo(areaSupplier.get(), matches.get(activeIdx)[0]);
    }
  }
}
