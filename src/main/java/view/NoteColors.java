package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;

/** The colours a note or tab can be marked with, and the dot that shows one. */
final class NoteColors {

  private static final Map<String, Color> COLORS = new LinkedHashMap<>();

  static {
    COLORS.put("red", new Color(0xE5484D));
    COLORS.put("orange", new Color(0xF76B15));
    COLORS.put("yellow", new Color(0xE2A336));
    COLORS.put("green", new Color(0x30A46C));
    COLORS.put("blue", new Color(0x0090FF));
    COLORS.put("purple", new Color(0x8E4EC6));
  }

  private NoteColors() {}

  static List<String> names() {
    return List.copyOf(COLORS.keySet());
  }

  /** The colour for a stored name; {@code null} for none or an unknown name. */
  static Color of(String name) {
    return name == null ? null : COLORS.get(name);
  }

  /** A small filled circle; {@code null} when there is no colour. */
  static Icon dot(String name, int size) {
    Color color = of(name);
    if (color == null) {
      return null;
    }
    return new Icon() {
      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y, size, size);
        g2.dispose();
      }

      @Override
      public int getIconWidth() {
        return size;
      }

      @Override
      public int getIconHeight() {
        return size;
      }
    };
  }
}
