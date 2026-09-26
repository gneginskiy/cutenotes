package view;

import static view.Messages.tr;

import java.util.function.Consumer;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import model.Theme;
import model.ThemePreset;
import model.ThemePresets;

class OptionsDialog extends JDialog {

  private final JColorChooser chooser = new JColorChooser();
  private final ThemePreview preview = new ThemePreview();
  private final ColorPalette palette = new ColorPalette(chooser, this::updatePreview);
  private final OptionsFields fields = new OptionsFields(this::updatePreview);
  private final PresetRow presets = new PresetRow(this::pickPreset);
  private final Consumer<Theme> onApply;

  OptionsDialog(JFrame owner, Consumer<Theme> onApply, AppSettingsPanel.Hooks hooks) {
    super(owner, tr("options.title"), false);
    this.onApply = onApply;
    ColorChoosers.compact(chooser);
    JTabbedPane pages = new JTabbedPane();
    pages.addTab(
        tr("options.tabTheme"),
        OptionsForm.build(
            presets,
            preview,
            new OptionsForm.Colors(
                palette.bgField(), palette.fgField(), palette.caretField(), palette.codeField()),
            chooser,
            fields,
            new OptionsForm.Actions(this::reset, this::hideFast, this::applyAndClose)));
    pages.addTab(tr("options.tabApp"), AppSettingsPanel.build(hooks));
    setContentPane(pages);
    Dialogs.bindEscape(this, this::hideFast);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    pack();
    WindowPlacement.centerOnOwner(this);
    UiPrimer.prime(getContentPane());
  }

  void openFor(Theme current) {
    palette.load(current.bg(), current.fg(), current.caret(), current.codeColor());
    fields.load(current);
    updatePreview();
    setVisible(true);
  }

  private void hideFast() {
    setVisible(false);
  }

  private void updatePreview() {
    Theme t = currentTheme();
    preview.render(t);
    presets.highlight(t);
  }

  /** Loads a built-in look into the form; the size, title and window settings stay as they are. */
  private void pickPreset(ThemePreset preset) {
    palette.load(preset.bg(), preset.fg(), null, preset.codeColor());
    fields.selectFont(preset.fontFamily(FontFamilies.installed()));
    updatePreview();
  }

  private Theme currentTheme() {
    return fields.compose(palette.bg(), palette.fg(), palette.caret(), palette.code());
  }

  private void reset() {
    fields.load(Theme.DEFAULT);
    pickPreset(ThemePresets.PAPER);
  }

  private void applyAndClose() {
    Theme t = currentTheme();
    setVisible(false);
    SwingUtilities.invokeLater(() -> onApply.accept(t));
  }
}
