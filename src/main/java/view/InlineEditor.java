package view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

final class InlineEditor {

  private InlineEditor() {}

  static JTextField create(
      String initial,
      Font font,
      UiPalette palette,
      BiConsumer<JTextField, String> onCommit,
      Consumer<JTextField> onCancel) {
    JTextField field = new JTextField(initial);
    field.setBorder(BorderFactory.createEmptyBorder());
    field.setOpaque(false);
    field.setForeground(palette.fg());
    field.setCaretColor(palette.accent());
    field.setSelectionColor(palette.selection());
    field.setSelectedTextColor(palette.fg());
    field.setFont(font);
    field.selectAll();
    field.addActionListener(a -> onCommit.accept(field, field.getText()));
    attachCommitOnBlur(field, onCommit);
    attachCancelOnEscape(field, onCancel);
    attachAutoResize(field);
    resize(field);
    return field;
  }

  private static void attachCommitOnBlur(
      JTextField field, BiConsumer<JTextField, String> onCommit) {
    field.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            onCommit.accept(field, field.getText());
          }
        });
  }

  private static void attachCancelOnEscape(JTextField field, Consumer<JTextField> onCancel) {
    field.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
              onCancel.accept(field);
              e.consume();
            }
          }
        });
  }

  private static void attachAutoResize(JTextField field) {
    field.getDocument().addDocumentListener(DocumentChanges.on(() -> resize(field)));
  }

  private static void resize(JTextField field) {
    FontMetrics fm = field.getFontMetrics(field.getFont());
    String text = field.getText().isEmpty() ? " " : field.getText();
    int w = fm.stringWidth(text) + 8;
    int h = fm.getHeight() + 2;
    Dimension size = new Dimension(w, h);
    field.setPreferredSize(size);
    field.setMaximumSize(size);
    field.setMinimumSize(size);
    field.revalidate();
  }
}
