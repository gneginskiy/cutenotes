package view;

import java.awt.BorderLayout;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Inline renaming of a tab: the title label is swapped for a text field and back. */
final class ChipEditing {

  private final JPanel chip;
  private JTextField editor;

  ChipEditing(JPanel chip) {
    this.chip = chip;
  }

  boolean active() {
    return editor != null;
  }

  void start(JLabel title, UiPalette palette, Consumer<String> done) {
    if (editor != null) {
      return;
    }
    editor =
        InlineEditor.create(
            title.getText(),
            title.getFont(),
            palette,
            (f, text) -> finish(title, done, text),
            f -> finish(title, done, null));
    chip.remove(title);
    chip.add(editor, BorderLayout.CENTER);
    chip.revalidate();
    chip.repaint();
    editor.requestFocusInWindow();
  }

  /** Commit (Enter, focus lost) and cancel (Esc) may both fire: only the first one counts. */
  private void finish(JLabel title, Consumer<String> done, String text) {
    if (editor == null) {
      return;
    }
    chip.remove(editor);
    editor = null;
    chip.add(title, BorderLayout.CENTER);
    chip.revalidate();
    chip.repaint();
    done.accept(text);
  }
}
