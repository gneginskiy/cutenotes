package view;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import lombok.SneakyThrows;

final class LineOps {

  private LineOps() {}

  static void install(JTextArea area) {
    int mask = ShortcutMask.menu();
    bind(area, KeyEvent.VK_X, mask, LineOps::cutLine);
    bind(area, KeyEvent.VK_C, mask, LineOps::copyLine);
    bind(area, KeyEvent.VK_D, mask, LineOps::duplicateLine);
  }

  private static void bind(JTextArea area, int key, int mod, Consumer<JTextArea> action) {
    String name = "line-op-" + key;
    area.getInputMap().put(KeyStroke.getKeyStroke(key, mod), name);
    area.getActionMap()
        .put(
            name,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                action.accept(area);
              }
            });
  }

  @SneakyThrows
  private static void cutLine(JTextArea area) {
    if (area.getSelectedText() == null) {
      int line = area.getLineOfOffset(area.getCaretPosition());
      area.select(area.getLineStartOffset(line), area.getLineEndOffset(line));
    }
    area.cut();
  }

  @SneakyThrows
  private static void copyLine(JTextArea area) {
    if (area.getSelectedText() != null) {
      area.copy();
      return;
    }
    int line = area.getLineOfOffset(area.getCaretPosition());
    int start = area.getLineStartOffset(line);
    int end = area.getLineEndOffset(line);
    String text = area.getText(start, end - start);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
  }

  @SneakyThrows
  private static void duplicateLine(JTextArea area) {
    int caret = area.getCaretPosition();
    int line = area.getLineOfOffset(caret);
    int start = area.getLineStartOffset(line);
    int end = area.getLineEndOffset(line);
    String lineText = area.getText(start, end - start);
    int col = caret - start;
    boolean noNl = !lineText.endsWith("\n");
    area.insert(noNl ? "\n" + lineText : lineText, end);
    area.setCaretPosition(end + (noNl ? 1 : 0) + col);
  }
}
