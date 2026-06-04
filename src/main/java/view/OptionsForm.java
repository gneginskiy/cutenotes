package view;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

final class OptionsForm {

  record Actions(Runnable reset, Runnable cancel, Runnable ok) {}

  record Colors(ColorField bg, ColorField fg, ColorField caret, ColorField code) {}

  private OptionsForm() {}

  static JPanel build(
      JComponent preview,
      Colors colors,
      JColorChooser chooser,
      JComboBox<String> fontBox,
      JSpinner sizeSpin,
      JTextField titleField,
      JCheckBox alwaysOnTop,
      Actions actions) {
    JPanel root = new JPanel(new BorderLayout(0, 8));
    root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    root.add(buildTop(preview, colors), BorderLayout.NORTH);
    root.add(chooser, BorderLayout.CENTER);
    root.add(buildBottom(fontBox, sizeSpin, titleField, alwaysOnTop, actions), BorderLayout.SOUTH);
    return root;
  }

  private static JPanel buildTop(JComponent preview, Colors colors) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(new JScrollPane(preview));
    Box row = Box.createHorizontalBox();
    row.add(new JLabel("Editing: "));
    row.add(colors.bg());
    row.add(Box.createHorizontalStrut(8));
    row.add(colors.fg());
    row.add(Box.createHorizontalStrut(8));
    row.add(colors.caret());
    row.add(Box.createHorizontalStrut(8));
    row.add(colors.code());
    row.add(Box.createHorizontalGlue());
    p.add(row);
    return p;
  }

  private static JPanel buildBottom(
      JComboBox<String> fontBox,
      JSpinner sizeSpin,
      JTextField titleField,
      JCheckBox alwaysOnTop,
      Actions actions) {
    JPanel p = new JPanel(new BorderLayout(0, 8));
    Box top = Box.createVerticalBox();
    Box title = Box.createHorizontalBox();
    title.add(new JLabel("Title: "));
    title.add(titleField);
    top.add(title);
    top.add(Box.createVerticalStrut(6));
    Box font = Box.createHorizontalBox();
    font.add(new JLabel("Font: "));
    font.add(fontBox);
    font.add(Box.createHorizontalStrut(8));
    font.add(new JLabel("Size: "));
    font.add(sizeSpin);
    top.add(font);
    top.add(Box.createVerticalStrut(6));
    Box aot = Box.createHorizontalBox();
    aot.add(alwaysOnTop);
    aot.add(Box.createHorizontalGlue());
    top.add(aot);
    p.add(top, BorderLayout.NORTH);
    JPanel buttons = new JPanel();
    buttons.add(btn("Reset", actions.reset()));
    buttons.add(btn("Cancel", actions.cancel()));
    buttons.add(btn("OK", actions.ok()));
    p.add(buttons, BorderLayout.SOUTH);
    return p;
  }

  private static JButton btn(String label, Runnable action) {
    JButton b = new JButton(label);
    b.addActionListener(e -> action.run());
    return b;
  }
}
