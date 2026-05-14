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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class InlineEditor {

  private InlineEditor() {}

  static JTextField create(
      String initial, BiConsumer<JTextField, String> onCommit, Consumer<JTextField> onCancel) {
    JTextField field = new JTextField(initial);
    field.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    field.setBackground(TabHeader.ACTIVE);
    field.setForeground(TabHeader.ACTIVE_FG);
    field.setCaretColor(Colors.inverse(TabHeader.ACTIVE));
    field.setFont(field.getFont().deriveFont(Font.BOLD));
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
    field
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                resize(field);
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                resize(field);
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                resize(field);
              }
            });
  }

  private static void resize(JTextField field) {
    FontMetrics fm = field.getFontMetrics(field.getFont());
    String text = field.getText().isEmpty() ? " " : field.getText();
    int w = fm.stringWidth(text) + 36;
    int h = fm.getHeight() + 12;
    Dimension size = new Dimension(w, h);
    field.setPreferredSize(size);
    field.setMaximumSize(size);
    field.setMinimumSize(size);
    field.revalidate();
  }
}
