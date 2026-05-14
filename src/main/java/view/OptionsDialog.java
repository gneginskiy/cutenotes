package view;

import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import model.Theme;

class OptionsDialog extends JDialog {

  private final JColorChooser chooser = new JColorChooser();
  private final JComboBox<String> fontBox = new JComboBox<>(FontFamilies.all());
  private final JSpinner sizeSpin = Spinners.intRange(12, 6, 72);
  private final JTextField titleField = new JTextField();
  private final JCheckBox alwaysOnTopBox = new JCheckBox("Always on top", true);
  private final JTextArea preview = new JTextArea("Preview\nThe quick brown fox\n123 456 789");
  private final ColorPalette palette = new ColorPalette(chooser, this::updatePreview);
  private final Consumer<Theme> onApply;

  OptionsDialog(JFrame owner, Consumer<Theme> onApply) {
    super(owner, "Options", false);
    this.onApply = onApply;
    ColorChoosers.compact(chooser);
    preview.setEditable(false);
    preview.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    preview.setRows(3);
    fontBox.addActionListener(e -> updatePreview());
    sizeSpin.addChangeListener(e -> updatePreview());
    setContentPane(
        OptionsForm.build(
            preview,
            new OptionsForm.Colors(palette.bgField(), palette.fgField(), palette.caretField()),
            chooser,
            fontBox,
            sizeSpin,
            titleField,
            alwaysOnTopBox,
            new OptionsForm.Actions(this::reset, this::hideFast, this::applyAndClose)));
    Dialogs.bindEscape(this, this::hideFast);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    pack();
    WindowPlacement.centerOnOwner(this);
    UiPrimer.prime(getContentPane());
  }

  void openFor(Theme current) {
    palette.load(current.bg(), current.fg(), current.caret());
    fontBox.setSelectedItem(FontFamilies.resolve(current.fontFamily()));
    sizeSpin.setValue(current.fontSize());
    titleField.setText(current.title() == null ? "" : current.title());
    alwaysOnTopBox.setSelected(current.alwaysOnTop());
    updatePreview();
    setVisible(true);
  }

  private void hideFast() {
    setVisible(false);
  }

  private void updatePreview() {
    TextAreaFactory.applyTheme(preview, currentTheme());
    preview.getCaret().setVisible(false);
    preview.getCaret().setVisible(true);
  }

  private Theme currentTheme() {
    String title = titleField.getText().trim();
    return new Theme(
        palette.bg(),
        palette.fg(),
        palette.caret(),
        (String) fontBox.getSelectedItem(),
        (Integer) sizeSpin.getValue(),
        title.isEmpty() ? null : title,
        alwaysOnTopBox.isSelected());
  }

  private void reset() {
    palette.load(Theme.DEFAULT.bg(), Theme.DEFAULT.fg(), Theme.DEFAULT.caret());
    fontBox.setSelectedItem(FontFamilies.resolve(Theme.DEFAULT.fontFamily()));
    sizeSpin.setValue(Theme.DEFAULT.fontSize());
    titleField.setText("");
    alwaysOnTopBox.setSelected(Theme.DEFAULT.alwaysOnTop());
    updatePreview();
  }

  private void applyAndClose() {
    Theme t = currentTheme();
    setVisible(false);
    SwingUtilities.invokeLater(() -> onApply.accept(t));
  }
}
