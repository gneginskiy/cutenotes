package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.Highlighter;

import markdown.Links;
import markdown.Tags;

/**
 * Links are underlined and open with Cmd/Ctrl+click; {@code #tags} get a soft pill. Both are drawn
 * as highlights, so the note's text and its undo history are never touched.
 */
final class EditorDecorations {

  static final String LAYER = "decorations";
  private static final int DELAY_MS = 120;

  private EditorDecorations() {}

  static void install(NoteEditor pane) {
    Timer refresh = new Timer(DELAY_MS, e -> refresh(pane));
    refresh.setRepeats(false);
    pane.getDocument().addDocumentListener(DocumentChanges.on(refresh::restart));
    pane.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            boolean modifier = (e.getModifiersEx() & ShortcutMask.menu()) != 0;
            if (modifier && SwingUtilities.isLeftMouseButton(e)) {
              open(linkAt(pane, pane.viewToModel2D(e.getPoint())));
            }
          }
        });
  }

  /** Re-marks all links and tags with the current theme's colours. */
  static void refresh(NoteEditor pane) {
    String text = LineOps.docText(pane);
    UiPalette p = UiPalette.current();
    Highlighter.HighlightPainter link = DecorationPainters.underline(p.accent());
    Highlighter.HighlightPainter tag =
        DecorationPainters.pill(Colors.blend(p.bg(), p.accent(), p.dark() ? 0.3f : 0.16f));
    List<Highlights.Mark> marks = new ArrayList<>();
    for (int[] r : Links.find(text)) {
      marks.add(new Highlights.Mark(r[0], r[1], link));
    }
    for (int[] r : Tags.find(text)) {
      marks.add(new Highlights.Mark(r[0], r[1], tag));
    }
    Highlights.set(pane, LAYER, marks);
  }

  /** The link at a document offset, or null. */
  static String linkAt(NoteEditor pane, int offset) {
    return offset < 0 ? null : Links.at(LineOps.docText(pane), offset);
  }

  static void open(String link) {
    Desktops.browse(link);
  }
}
