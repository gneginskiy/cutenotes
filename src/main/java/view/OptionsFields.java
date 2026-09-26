package view;

import static view.Messages.tr;

import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import model.Theme;

/** The text and window inputs of the Options dialog: font, size, window title, always on top. */
final class OptionsFields {

  private final JComboBox<String> fontBox = new JComboBox<>(FontFamilies.all());
  private final JSpinner sizeSpin = Spinners.intRange(12, 6, 72);
  private final JTextField titleField = new JTextField();
  private final JCheckBox alwaysOnTop = new JCheckBox(tr("options.alwaysOnTop"), true);

  OptionsFields(Runnable onChange) {
    fontBox.addActionListener(e -> onChange.run());
    sizeSpin.addChangeListener(e -> onChange.run());
  }

  JComboBox<String> fontBox() {
    return fontBox;
  }

  JSpinner sizeSpin() {
    return sizeSpin;
  }

  JTextField titleField() {
    return titleField;
  }

  JCheckBox alwaysOnTop() {
    return alwaysOnTop;
  }

  void load(Theme t) {
    selectFont(FontFamilies.resolve(t.fontFamily()));
    sizeSpin.setValue(t.fontSize());
    titleField.setText(t.title() == null ? "" : t.title());
    alwaysOnTop.setSelected(t.alwaysOnTop());
  }

  void selectFont(String family) {
    fontBox.setSelectedItem(family);
  }

  /** The theme described by these inputs plus the given colours. */
  Theme compose(Color bg, Color fg, Color caret, Color code) {
    String title = titleField.getText().trim();
    return new Theme(
        bg,
        fg,
        caret,
        code,
        (String) fontBox.getSelectedItem(),
        (Integer) sizeSpin.getValue(),
        title.isEmpty() ? null : title,
        alwaysOnTop.isSelected());
  }
}
