package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.View;

import markdown.LineKind;

/**
 * A run of text in a note. Words too long for the line may wrap mid-word (no minimum width), and
 * the line's Markdown shape is shown live without touching the document: {@code #} headings are
 * larger and bold, done tasks ({@code - [x]}) are struck through and muted.
 */
final class NoteLabelView extends LabelView {

  NoteLabelView(Element elem) {
    super(elem);
  }

  @Override
  public float getMinimumSpan(int axis) {
    return axis == View.X_AXIS ? 0 : super.getMinimumSpan(axis);
  }

  @Override
  public Font getFont() {
    Font font = super.getFont();
    LineKind kind = kind();
    return kind.heading() ? font.deriveFont(Font.BOLD, font.getSize2D() * kind.scale()) : font;
  }

  @Override
  public boolean isStrikeThrough() {
    return super.isStrikeThrough() || kind() == LineKind.DONE;
  }

  @Override
  public Color getForeground() {
    Color fg = super.getForeground();
    Container host = getContainer();
    if (kind() != LineKind.DONE || host == null || fg == null) {
      return fg;
    }
    return Colors.blend(fg, host.getBackground(), 0.5f);
  }

  /** The kind of the paragraph this run belongs to, read from its first few characters. */
  LineKind kind() {
    Element paragraph = getElement().getParentElement();
    if (paragraph == null) {
      return LineKind.PLAIN;
    }
    int start = paragraph.getStartOffset();
    int length = Math.min(paragraph.getEndOffset() - start, NoteParagraphView.PROBE);
    try {
      return LineKind.of(getDocument().getText(start, length));
    } catch (BadLocationException e) {
      return LineKind.PLAIN;
    }
  }
}
