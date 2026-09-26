package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class EditorMenuTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @Test
  void offersLinkActionsOnlyOnALinkAndClipboardByState() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("open https://example.com now");

          List<String> onLink = labels(EditorMenu.build(e, 8));
          List<String> onText = labels(EditorMenu.build(e, 1));

          assertEquals("Open link", onLink.get(0));
          assertFalse(onText.contains("Open link"));
          assertTrue(onText.contains("Paste as plain text"));
          assertFalse(item(EditorMenu.build(e, 1), "Cut").isEnabled());
          e.select(0, 4);
          assertTrue(item(EditorMenu.build(e, 1), "Cut").isEnabled());
        });
  }

  @Test
  void formatSubmenuTogglesStyles() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("word");
          e.select(0, 4);
          JMenu format = EditorMenu.formatMenu(e, ShortcutMask.menu());

          format.getItem(0).doClick();

          assertEquals("**word**", e.markdown());
          assertEquals(InlineStyle.values().length, format.getItemCount());
        });
  }

  @Test
  void plainInsertIgnoresMarkdownAndFormatting() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("**bold**");
          e.setCaretPosition(3);
          EditorClipboard.insertPlain(e, "*x* ![](images/a.png)");
          assertEquals("**bol**\\*x\\* ![](images/a.png)**d**", e.markdown());
        });
  }

  private static JMenuItem item(JPopupMenu menu, String label) {
    for (java.awt.Component c : menu.getComponents()) {
      if (c instanceof JMenuItem i && label.equals(i.getText())) {
        return i;
      }
    }
    throw new AssertionError("no item " + label);
  }

  private static List<String> labels(JPopupMenu menu) {
    List<String> out = new ArrayList<>();
    for (java.awt.Component c : menu.getComponents()) {
      if (c instanceof JMenuItem i) {
        out.add(i.getText());
      }
    }
    return out;
  }
}
