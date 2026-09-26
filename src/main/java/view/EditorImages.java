package view;

import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/** Wires clipboard-image paste and the {@link ImageMouse} interactions onto a pane. */
final class EditorImages {

  private EditorImages() {}

  static void install(NoteEditor pane) {
    if (!GraphicsEnvironment.isHeadless()) {
      pane.setDragEnabled(true);
    }
    int mask = ShortcutMask.menu();
    KeyBindings.bindFocused(
        pane,
        KeyStroke.getKeyStroke(KeyEvent.VK_V, mask),
        "image-paste",
        () -> NoteImages.paste(pane));
    KeyBindings.bindFocused(
        pane,
        KeyStroke.getKeyStroke(KeyEvent.VK_V, mask | InputEvent.SHIFT_DOWN_MASK),
        "paste-plain",
        () -> EditorClipboard.pastePlain(pane));
    ImageMouse mouse = new ImageMouse(pane);
    pane.addMouseListener(mouse);
    pane.addMouseMotionListener(mouse);
  }
}
