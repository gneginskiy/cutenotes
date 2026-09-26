package view;

import static view.Messages.tr;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

/** Right-click menu of the note text: clipboard, formatting, checkbox and link actions. */
final class EditorMenu {

  private EditorMenu() {}

  static void install(NoteEditor pane) {
    pane.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            maybeShow(pane, e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybeShow(pane, e);
          }
        });
  }

  private static void maybeShow(NoteEditor pane, MouseEvent e) {
    if (e.isPopupTrigger() && ImageActions.imageAt(pane, e.getPoint()) < 0) {
      show(pane, e.getPoint());
    }
  }

  static JPopupMenu build(NoteEditor pane, int offset) {
    JPopupMenu menu = new JPopupMenu();
    String link = EditorDecorations.linkAt(pane, offset);
    if (link != null) {
      menu.add(item(tr("editor.openLink"), null, () -> EditorDecorations.open(link)));
      menu.add(item(tr("editor.copyLink"), null, () -> copy(link)));
      menu.addSeparator();
    }
    int mask = ShortcutMask.menu();
    addClipboard(menu, pane, mask);
    menu.addSeparator();
    menu.add(formatMenu(pane, mask));
    menu.add(
        item(
            tr("menu.format.checkbox"),
            key(KeyEvent.VK_ENTER, mask),
            () -> EditorCheckboxes.toggle(pane, pane.getCaretPosition())));
    MenuTheme.stylePopup(menu, UiPalette.current());
    return menu;
  }

  private static void addClipboard(JPopupMenu menu, NoteEditor pane, int mask) {
    boolean selection = pane.getSelectionStart() != pane.getSelectionEnd();
    menu.add(
            item(
                tr("menu.edit.cut"), key(KeyEvent.VK_X, mask), () -> EditorClipboard.cutLine(pane)))
        .setEnabled(selection);
    menu.add(
            item(
                tr("menu.edit.copy"),
                key(KeyEvent.VK_C, mask),
                () -> EditorClipboard.copyLine(pane)))
        .setEnabled(selection);
    menu.add(item(tr("menu.edit.paste"), key(KeyEvent.VK_V, mask), () -> NoteImages.paste(pane)));
    menu.add(
        item(
            tr("menu.edit.pastePlain"),
            key(KeyEvent.VK_V, mask | InputEvent.SHIFT_DOWN_MASK),
            () -> EditorClipboard.pastePlain(pane)));
    menu.add(item(tr("menu.edit.selectAll"), key(KeyEvent.VK_A, mask), pane::selectAll));
  }

  static JMenu formatMenu(NoteEditor pane, int mask) {
    JMenu format = new JMenu(tr("menu.format"));
    for (InlineStyle style : InlineStyle.values()) {
      format.add(
          item(tr(style.labelKey), style.keyStroke(mask), () -> EditorFormat.toggle(pane, style)));
    }
    return format;
  }

  private static void show(NoteEditor pane, Point p) {
    int offset = pane.viewToModel2D(p);
    boolean insideSelection = offset >= pane.getSelectionStart() && offset < pane.getSelectionEnd();
    if (!insideSelection && offset >= 0) {
      pane.setCaretPosition(offset);
    }
    build(pane, offset).show(pane, p.x, p.y);
  }

  static JMenuItem item(String label, KeyStroke accelerator, Runnable action) {
    JMenuItem item = new JMenuItem(label);
    item.setAccelerator(accelerator);
    item.addActionListener(e -> action.run());
    return item;
  }

  private static KeyStroke key(int code, int modifiers) {
    return KeyStroke.getKeyStroke(code, modifiers);
  }

  private static void copy(String text) {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
  }
}
