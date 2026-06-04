package view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;

/** Captures and re-inserts styled text runs so line moves keep formatting and inline images. */
final class StyledRuns {

  record Run(String text, AttributeSet attrs) {}

  private StyledRuns() {}

  @SneakyThrows
  static List<Run> capture(StyledDocument doc, int start, int end) {
    List<Run> runs = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();
    AttributeSet current = null;
    for (int i = start; i < end; i++) {
      AttributeSet attrs = doc.getCharacterElement(i).getAttributes();
      if (current == null) {
        current = attrs;
      } else if (!attrs.isEqual(current)) {
        runs.add(new Run(buffer.toString(), current.copyAttributes()));
        buffer.setLength(0);
        current = attrs;
      }
      buffer.append(doc.getText(i, 1));
    }
    if (buffer.length() > 0) {
      runs.add(new Run(buffer.toString(), current.copyAttributes()));
    }
    return runs;
  }

  @SneakyThrows
  static int insert(StyledDocument doc, int pos, List<Run> runs) {
    int at = pos;
    for (Run run : runs) {
      doc.insertString(at, run.text(), run.attrs());
      at += run.text().length();
    }
    return at;
  }

  static List<Run> trimTrailingNewline(List<Run> runs) {
    if (runs.isEmpty()) {
      return runs;
    }
    Run last = runs.get(runs.size() - 1);
    if (!last.text().endsWith("\n")) {
      return runs;
    }
    List<Run> copy = new ArrayList<>(runs);
    copy.set(
        copy.size() - 1, new Run(last.text().substring(0, last.text().length() - 1), last.attrs()));
    return copy;
  }
}
