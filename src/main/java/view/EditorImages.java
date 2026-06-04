package view;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/** Wires clipboard-image paste and the {@link ImageMouse} interactions onto a pane. */
final class EditorImages {

  private EditorImages() {}

  static void install(NoteEditor pane) {
    if (!GraphicsEnvironment.isHeadless()) {
      pane.setDragEnabled(true);
    }
    String name = "image-paste";
    pane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ShortcutMask.menu()), name);
    pane.getActionMap()
        .put(
            name,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                NoteImages.paste(pane);
              }
            });
    ImageMouse mouse = new ImageMouse(pane);
    pane.addMouseListener(mouse);
    pane.addMouseMotionListener(mouse);
  }
}
