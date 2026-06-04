package view;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;

/**
 * Cut/copy of lines and selections that preserves inline images by using Markdown on the clipboard.
 */
final class EditorClipboard {

  private EditorClipboard() {}

  @SneakyThrows
  static void cutLine(JTextComponent area) {
    int start;
    int end;
    if (area.getSelectedText() == null) {
      String text = LineOps.docText(area);
      int caret = area.getCaretPosition();
      start = Lines.start(text, caret);
      end = Lines.endInclusive(text, caret);
    } else {
      start = area.getSelectionStart();
      end = area.getSelectionEnd();
    }
    StyledDocument doc = (StyledDocument) area.getDocument();
    if (DocumentMarkdown.hasImage(doc, start, end)) {
      toClipboard(DocumentMarkdown.toMarkdown(doc, start, end));
      doc.remove(start, end - start);
    } else {
      area.select(start, end);
      area.cut();
    }
  }

  static void copyLine(JTextComponent area) {
    StyledDocument doc = (StyledDocument) area.getDocument();
    if (area.getSelectedText() != null) {
      copyRange(area, doc, area.getSelectionStart(), area.getSelectionEnd(), null);
      return;
    }
    String text = LineOps.docText(area);
    int caret = area.getCaretPosition();
    int start = Lines.start(text, caret);
    int end = Lines.endInclusive(text, caret);
    copyRange(area, doc, start, end, text.substring(start, end));
  }

  private static void copyRange(
      JTextComponent area, StyledDocument doc, int start, int end, String plainFallback) {
    if (DocumentMarkdown.hasImage(doc, start, end)) {
      toClipboard(DocumentMarkdown.toMarkdown(doc, start, end));
    } else if (plainFallback == null) {
      area.copy();
    } else {
      toClipboard(plainFallback);
    }
  }

  private static void toClipboard(String value) {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
  }
}
