package view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.ViewFactory;

import markdown.LineKind;

/**
 * A paragraph of a note. Line spacing is a fraction of each line's height, so under a 300 px image
 * it used to add a 45 px gap: paragraphs holding an image get no extra spacing. A paragraph made of
 * code only is drawn as a block with a full-width background, and a change of the line's Markdown
 * kind (typing {@code # } or checking a task) re-lays out all of its runs.
 */
class NoteParagraphView extends ParagraphView {

  /** Characters enough to tell the line kind ({@code " - [x] "}). */
  static final int PROBE = 32;

  private static final int BLOCK_ARC = 8;

  private boolean hasImage;
  private LineKind kind = LineKind.PLAIN;

  NoteParagraphView(Element elem) {
    super(elem);
  }

  @Override
  protected void setPropertiesFromAttributes() {
    super.setPropertiesFromAttributes();
    hasImage = containsImage();
    if (hasImage) {
      setLineSpacing(0);
    }
  }

  @Override
  public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    super.insertUpdate(e, a, f);
    refresh();
  }

  @Override
  public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    super.removeUpdate(e, a, f);
    refresh();
  }

  @Override
  public void paint(Graphics g, Shape allocation) {
    if (allCode()) {
      Rectangle r = allocation.getBounds();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(EditorFormat.codeBackground(ThemeHolder.current()));
      g2.fillRoundRect(r.x - 6, r.y, r.width + 12, r.height, BLOCK_ARC, BLOCK_ARC);
      g2.dispose();
    }
    super.paint(g, allocation);
  }

  private void refresh() {
    if (containsImage() != hasImage) {
      setPropertiesFromAttributes();
      preferenceChanged(null, true, true);
    }
    LineKind now = currentKind();
    if (now != kind) {
      kind = now;
      for (int i = 0; i < getViewCount(); i++) {
        getView(i).preferenceChanged(null, true, true);
      }
      preferenceChanged(null, true, true);
    }
  }

  private LineKind currentKind() {
    Element p = getElement();
    int length = Math.min(p.getEndOffset() - p.getStartOffset(), PROBE);
    try {
      return LineKind.of(p.getDocument().getText(p.getStartOffset(), length));
    } catch (BadLocationException e) {
      return LineKind.PLAIN;
    }
  }

  /** Whether every character of the paragraph (except its line break) is code. */
  private boolean allCode() {
    Element p = getElement();
    boolean any = false;
    for (int i = 0; i < p.getElementCount(); i++) {
      Element run = p.getElement(i);
      boolean lineBreakOnly =
          run.getEndOffset() - run.getStartOffset() == 1 && i == p.getElementCount() - 1;
      if (EditorFormat.isCode(run.getAttributes())) {
        any = true;
      } else if (!lineBreakOnly) {
        return false;
      }
    }
    return any;
  }

  private boolean containsImage() {
    Element paragraph = getElement();
    for (int i = 0; i < paragraph.getElementCount(); i++) {
      if (StyleConstants.getIcon(paragraph.getElement(i).getAttributes()) != null) {
        return true;
      }
    }
    return false;
  }
}
