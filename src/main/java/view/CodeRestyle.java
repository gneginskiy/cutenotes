package view;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import model.Theme;

/**
 * Re-colours code sections after a theme change. Works per element run instead of per character and
 * skips runs that already match: zooming (font size only) used to rewrite every code character as a
 * separate undoable edit, flooding the undo history and firing thousands of document events.
 */
final class CodeRestyle {

  private CodeRestyle() {}

  static void apply(StyledDocument doc, Theme t) {
    SimpleAttributeSet on = new SimpleAttributeSet();
    EditorFormat.styleCode(on, true, t);
    SimpleAttributeSet off = new SimpleAttributeSet();
    EditorFormat.styleCode(off, false, t);
    int i = 0;
    while (i < doc.getLength()) {
      Element run = doc.getCharacterElement(i);
      int end = Math.min(run.getEndOffset(), doc.getLength());
      AttributeSet target = targetFor(run.getAttributes(), on, off);
      if (target != null && !run.getAttributes().containsAttributes(target)) {
        doc.setCharacterAttributes(i, end - i, target, false);
      }
      i = end;
    }
  }

  private static AttributeSet targetFor(AttributeSet a, AttributeSet on, AttributeSet off) {
    if (a.getAttribute(EditorFormat.CODE) == Boolean.TRUE) {
      return on;
    }
    boolean coloured =
        a.isDefined(StyleConstants.Foreground) || a.isDefined(StyleConstants.Background);
    return coloured ? off : null;
  }
}
