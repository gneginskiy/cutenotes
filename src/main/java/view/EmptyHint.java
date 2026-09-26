package view;

import static view.Messages.tr;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;

/**
 * What an empty note shows until the first keystroke: where to start and the few shortcuts that
 * unlock the rest (the menu and tabs are hidden by default, so a newcomer would not find them).
 */
final class EmptyHint {

  private EmptyHint() {}

  static List<String> lines(boolean mac) {
    String cmd = mac ? "⌘" : "Ctrl+";
    return List.of(
        tr("editor.hint.title"),
        tr("editor.hint.line1", cmd + "T", cmd + "R"),
        tr("editor.hint.line2"));
  }

  static void paint(Graphics g, NoteEditor editor) {
    if (editor.getDocument().getLength() > 0) {
      return;
    }
    UiPalette p = UiPalette.current();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    Insets in = editor.getInsets();
    List<String> lines = lines(PlatformLook.MAC);
    Font title = editor.getFont();
    Font small = UiFonts.ui(Font.PLAIN, Math.max(11f, title.getSize2D() - 2f));
    g2.setColor(Colors.blend(p.muted(), p.bg(), 0.2f));
    g2.setFont(title);
    FontMetrics fm = g2.getFontMetrics();
    int y = in.top + fm.getAscent();
    g2.drawString(lines.get(0), in.left, y);
    g2.setFont(small);
    g2.setColor(Colors.blend(p.muted(), p.bg(), 0.35f));
    y += fm.getDescent() + g2.getFontMetrics().getHeight() + 6;
    for (String line : lines.subList(1, lines.size())) {
      g2.drawString(line, in.left, y);
      y += g2.getFontMetrics().getHeight() + 2;
    }
    g2.dispose();
  }
}
