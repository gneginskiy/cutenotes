package view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

/** Document edits behind {@link EditorLists}: indenting line starts and swapping one character. */
final class LineIndent {

  static final String STEP = "  ";

  private LineIndent() {}

  /**
   * Indents (+1) or outdents (-1) every line touched by {@code [selStart, selEnd]}; outdent removes
   * up to two spaces or one tab. Lines are edited bottom-up so earlier offsets stay valid.
   */
  static void apply(StyledDocument doc, String text, int selStart, int selEnd, int direction)
      throws BadLocationException {
    List<Integer> starts = lineStarts(text, selStart, selEnd);
    for (int i = starts.size() - 1; i >= 0; i--) {
      int start = starts.get(i);
      if (direction > 0) {
        doc.insertString(start, STEP, new SimpleAttributeSet());
      } else {
        int remove = outdentWidth(text, start);
        if (remove > 0) {
          doc.remove(start, remove);
        }
      }
    }
  }

  /** Replaces the character at {@code offset}, keeping its formatting. */
  static void replaceChar(StyledDocument doc, int offset, String replacement)
      throws BadLocationException {
    AttributeSet attrs = doc.getCharacterElement(offset).getAttributes().copyAttributes();
    doc.remove(offset, 1);
    doc.insertString(offset, replacement, attrs);
  }

  static List<Integer> lineStarts(String text, int selStart, int selEnd) {
    List<Integer> starts = new ArrayList<>();
    int last = selEnd > selStart && Lines.start(text, selEnd) == selEnd ? selEnd - 1 : selEnd;
    int start = Lines.start(text, selStart);
    while (true) {
      starts.add(start);
      int next = text.indexOf('\n', start);
      if (next < 0 || next >= last) {
        return starts;
      }
      start = next + 1;
    }
  }

  static int outdentWidth(String text, int start) {
    if (start < text.length() && text.charAt(start) == '\t') {
      return 1;
    }
    int width = 0;
    while (width < STEP.length()
        && start + width < text.length()
        && text.charAt(start + width) == ' ') {
      width++;
    }
    return width;
  }
}
