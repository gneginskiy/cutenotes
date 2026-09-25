package view;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import model.Theme;

/** A read-only sample of body text and a code section, rendered exactly as the editor would. */
final class ThemePreview extends JTextPane {

  ThemePreview() {
    setEditable(false);
    setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
  }

  /** The same typography as the note editor, so the preview shows what the notes will look like. */
  @Override
  protected EditorKit createDefaultEditorKit() {
    return new NoteEditorKit();
  }

  void render(Theme t) {
    EditorTheme.apply(this, t);
    StyledDocument doc = getStyledDocument();
    SimpleAttributeSet code = new SimpleAttributeSet();
    EditorFormat.styleCode(code, true, t);
    try {
      doc.remove(0, doc.getLength());
      doc.insertString(0, "The quick brown fox jumps over the lazy dog.\n", null);
      doc.insertString(doc.getLength(), "code = 42;", code);
    } catch (BadLocationException e) {
      throw new IllegalStateException(e);
    }
  }
}
