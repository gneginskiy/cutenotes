package view;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;
import markdown.MarkdownText;
import markdown.MdImage;
import markdown.MdNode;
import markdown.MdText;

/** Bridges a {@link StyledDocument} (with inline images) and its Markdown representation. */
final class DocumentMarkdown {

  private DocumentMarkdown() {}

  static String toMarkdown(StyledDocument doc) {
    return toMarkdown(doc, 0, doc.getLength());
  }

  /**
   * Walks the document run by run (one element at a time), not char by char: the per-char walk
   * allocated a String and did an element lookup for every character of every open note.
   */
  @SneakyThrows
  static String toMarkdown(StyledDocument doc, int start, int end) {
    List<MdNode> nodes = new ArrayList<>();
    StringBuilder run = new StringBuilder();
    boolean[] flags = null;
    int i = start;
    while (i < end) {
      Element element = doc.getCharacterElement(i);
      int stop = Math.min(element.getEndOffset(), end);
      AttributeSet a = element.getAttributes();
      if (ImageAttr.isImage(a)) {
        addText(nodes, run, flags);
        flags = null;
        for (int k = i; k < stop; k++) {
          nodes.add(new MdImage(ImageAttr.path(a), ImageAttr.width(a), ImageAttr.height(a)));
        }
      } else {
        boolean[] current = flagsOf(a);
        if (flags != null && !Arrays.equals(current, flags)) {
          addText(nodes, run, flags);
        }
        flags = current;
        run.append(doc.getText(i, stop - i));
      }
      i = stop;
    }
    addText(nodes, run, flags);
    return MarkdownText.write(nodes);
  }

  private static boolean[] flagsOf(AttributeSet a) {
    return new boolean[] {
      StyleConstants.isBold(a),
      StyleConstants.isItalic(a),
      StyleConstants.isUnderline(a),
      StyleConstants.isStrikeThrough(a),
      a.getAttribute(EditorFormat.CODE) == Boolean.TRUE
    };
  }

  static boolean hasImage(StyledDocument doc, int start, int end) {
    for (int i = start; i < end; i++) {
      if (ImageAttr.isImage(doc.getCharacterElement(i).getAttributes())) {
        return true;
      }
    }
    return false;
  }

  private static void addText(List<MdNode> nodes, StringBuilder run, boolean[] flags) {
    if (run.length() > 0 && flags != null) {
      nodes.add(new MdText(run.toString(), flags[0], flags[1], flags[2], flags[3], flags[4]));
      run.setLength(0);
    }
  }

  @SneakyThrows
  static void applyMarkdown(StyledDocument doc, String md) {
    doc.remove(0, doc.getLength());
    insertAt(doc, 0, md);
  }

  static int insertAt(StyledDocument doc, int offset, String md) {
    int pos = offset;
    MdImage previous = null;
    for (MdNode node : MarkdownText.parse(md)) {
      if (node instanceof MdText t) {
        pos = insertText(doc, pos, t);
        previous = null;
      } else if (node instanceof MdImage img) {
        if (!img.equals(previous)) {
          pos = insertImage(doc, pos, img);
          previous = img;
        }
      }
    }
    return pos;
  }

  @SneakyThrows
  private static int insertText(StyledDocument doc, int pos, MdText t) {
    SimpleAttributeSet attr = new SimpleAttributeSet();
    StyleConstants.setBold(attr, t.bold());
    StyleConstants.setItalic(attr, t.italic());
    StyleConstants.setUnderline(attr, t.underline());
    StyleConstants.setStrikeThrough(attr, t.strike());
    if (t.code()) {
      EditorFormat.styleCode(attr, true);
    }
    doc.insertString(pos, t.text(), attr);
    return pos + t.text().length();
  }

  @SneakyThrows
  private static int insertImage(StyledDocument doc, int pos, MdImage img) {
    BufferedImage source = NoteImages.load(img.path());
    if (source == null) {
      String literal = "![](" + img.path() + ")";
      doc.insertString(pos, literal, null);
      return pos + literal.length();
    }
    int width = img.width() > 0 ? img.width() : source.getWidth();
    int height = img.height() > 0 ? img.height() : source.getHeight();
    doc.insertString(pos, " ", ImageAttr.of(img.path(), source, width, height));
    return pos + 1;
  }
}
