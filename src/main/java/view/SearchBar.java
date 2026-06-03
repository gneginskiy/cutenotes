package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import model.Theme;

class SearchBar extends JPanel {

  private static final Color MATCH = new Color(255, 235, 60, 150);
  private static final Color ACTIVE = new Color(255, 150, 0, 220);

  private final JTextField input = new JTextField();
  private final SearchControls controls =
      new SearchControls(() -> step(-1), () -> step(1), this::runSearch, this::close);
  private final Supplier<JTextArea> areaSupplier;
  private final List<int[]> matches = new ArrayList<>();
  private final Highlighter.HighlightPainter matchPainter =
      new DefaultHighlighter.DefaultHighlightPainter(MATCH);
  private final Highlighter.HighlightPainter activePainter =
      new DefaultHighlighter.DefaultHighlightPainter(ACTIVE);
  private int activeIdx = -1;

  SearchBar(Supplier<JTextArea> areaSupplier) {
    super(new BorderLayout());
    this.areaSupplier = areaSupplier;
    setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    add(input, BorderLayout.CENTER);
    add(controls, BorderLayout.EAST);
    setVisible(false);
    applyTheme(ThemeHolder.current());
    wireInput();
  }

  void applyTheme(Theme t) {
    Color bg = Colors.darken(t.bg(), 18);
    Color fg = t.fg();
    setBackground(bg);
    setOpaque(true);
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.divider(t.bg())),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    input.setBackground(bg);
    input.setForeground(fg);
    input.setCaretColor(t.caret() != null ? t.caret() : Colors.inverse(bg));
    controls.applyForeground(fg);
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
    JTextArea area = areaSupplier.get();
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
    JTextArea area = areaSupplier.get();
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
      controls.setCounterText(input.getText().isEmpty() ? "" : "no matches");
    } else {
      controls.setCounterText((activeIdx + 1) + " of " + matches.size());
    }
  }

  private void scrollToActive() {
    if (activeIdx >= 0) {
      SearchEngine.scrollTo(areaSupplier.get(), matches.get(activeIdx)[0]);
    }
  }
}
