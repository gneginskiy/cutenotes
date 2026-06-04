package view;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/** Character attributes that turn a single placeholder char into an inline, resizable image. */
final class ImageAttr {

  static final String PATH = "cutenotes.image.path";
  static final String SOURCE = "cutenotes.image.source";
  static final String WIDTH = "cutenotes.image.width";
  static final String HEIGHT = "cutenotes.image.height";

  private ImageAttr() {}

  static boolean isImage(AttributeSet attrs) {
    return attrs.getAttribute(PATH) != null;
  }

  static SimpleAttributeSet of(String path, Image source, int width, int height) {
    int w = Math.max(1, width);
    int h = Math.max(1, height);
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    StyleConstants.setIcon(
        attrs, new ImageIcon(source.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
    attrs.addAttribute(PATH, path);
    attrs.addAttribute(SOURCE, source);
    attrs.addAttribute(WIDTH, w);
    attrs.addAttribute(HEIGHT, h);
    return attrs;
  }

  static String path(AttributeSet attrs) {
    return (String) attrs.getAttribute(PATH);
  }

  static Image source(AttributeSet attrs) {
    Object value = attrs.getAttribute(SOURCE);
    return value instanceof Image image ? image : null;
  }

  static int width(AttributeSet attrs) {
    return intValue(attrs.getAttribute(WIDTH));
  }

  static int height(AttributeSet attrs) {
    return intValue(attrs.getAttribute(HEIGHT));
  }

  private static int intValue(Object value) {
    return value instanceof Integer i ? i : 0;
  }
}
