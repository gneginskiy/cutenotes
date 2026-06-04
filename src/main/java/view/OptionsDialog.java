package view;

import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;
import model.Theme;

class OptionsDialog extends JDialog {

  private final JColorChooser chooser = new JColorChooser();
  private final JComboBox<String> fontBox = new JComboBox<>(FontFamilies.all());
  private final JSpinner sizeSpin = Spinners.intRange(12, 6, 72);
  private final JTextField titleField = new JTextField();
  private final JCheckBox alwaysOnTopBox = new JCheckBox("Always on top", true);
  private final JTextPane preview = new JTextPane();
  private final ColorPalette palette = new ColorPalette(chooser, this::updatePreview);
  private final Consumer<Theme> onApply;

  OptionsDialog(JFrame owner, Consumer<Theme> onApply) {
    super(owner, "Options", false);
    this.onApply = onApply;
    ColorChoosers.compact(chooser);
    preview.setEditable(false);
    preview.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    fontBox.addActionListener(e -> updatePreview());
    sizeSpin.addChangeListener(e -> updatePreview());
    setContentPane(
        OptionsForm.build(
            preview,
            new OptionsForm.Colors(
                palette.bgField(), palette.fgField(), palette.caretField(), palette.codeField()),
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
    palette.load(current.bg(), current.fg(), current.caret(), current.codeColor());
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

  @SneakyThrows
  private void updatePreview() {
    Theme t = currentTheme();
    EditorTheme.apply(preview, t);
    StyledDocument doc = preview.getStyledDocument();
    doc.remove(0, doc.getLength());
    doc.insertString(0, "The quick brown fox\n", null);
    SimpleAttributeSet code = new SimpleAttributeSet();
    EditorFormat.styleCode(code, true, t);
    doc.insertString(doc.getLength(), "code = 42;", code);
  }

  private Theme currentTheme() {
    String title = titleField.getText().trim();
    return new Theme(
        palette.bg(),
        palette.fg(),
        palette.caret(),
        palette.code(),
        (String) fontBox.getSelectedItem(),
        (Integer) sizeSpin.getValue(),
        title.isEmpty() ? null : title,
        alwaysOnTopBox.isSelected());
  }

  private void reset() {
    palette.load(
        Theme.DEFAULT.bg(), Theme.DEFAULT.fg(), Theme.DEFAULT.caret(), Theme.DEFAULT.codeColor());
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
