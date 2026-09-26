package view;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * The editor kit of notes (and of the Options preview, which must look the same):
 *
 * <ul>
 *   <li>words too long for the line (keys, hashes, URLs) wrap mid-word, like a plain text area.
 *       Swing's styled text only breaks at spaces, so such a word used to push the note sideways
 *       behind a horizontal scrollbar;
 *   <li>airy line spacing, set on the default style before any view exists. Set later, on a still
 *       empty document, Swing never refreshed the first paragraph and the first line stayed tight.
 * </ul>
 */
final class NoteEditorKit extends StyledEditorKit {

  /** Extra space under each line, as a fraction of the line height. */
  static final float LINE_SPACING = 0.15f;

  private static final ViewFactory FACTORY = NoteEditorKit::create;

  @Override
  public ViewFactory getViewFactory() {
    return FACTORY;
  }

  /**
   * Every paragraph inherits the spacing from the default style: it is neither an undoable edit nor
   * part of the saved Markdown.
   */
  @Override
  public Document createDefaultDocument() {
    Document doc = super.createDefaultDocument();
    if (doc instanceof StyledDocument styled) {
      StyleConstants.setLineSpacing(styled.getStyle(StyleContext.DEFAULT_STYLE), LINE_SPACING);
    }
    return doc;
  }

  /** The same views as {@link StyledEditorKit}, with a text view that may shrink to nothing. */
  private static View create(Element elem) {
    String kind = elem.getName();
    if (AbstractDocument.ParagraphElementName.equals(kind)) {
      return new NoteParagraphView(elem);
    }
    if (AbstractDocument.SectionElementName.equals(kind)) {
      return new BoxView(elem, View.Y_AXIS);
    }
    if (StyleConstants.ComponentElementName.equals(kind)) {
      return new ComponentView(elem);
    }
    if (StyleConstants.IconElementName.equals(kind)) {
      return new IconView(elem);
    }
    return new NoteLabelView(elem);
  }
}
