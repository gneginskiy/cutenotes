package view;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;

import model.Theme;
import model.ThemePreset;
import model.ThemePresets;

/** The row of built-in themes in Options; the one matching the edited colours is outlined. */
final class PresetRow extends JPanel {

  private final List<PresetSwatch> swatches = new ArrayList<>();

  PresetRow(Consumer<ThemePreset> onPick) {
    super(new FlowLayout(FlowLayout.LEFT, 6, 0));
    setOpaque(false);
    for (ThemePreset preset : ThemePresets.ALL) {
      PresetSwatch swatch = new PresetSwatch(preset, onPick);
      swatches.add(swatch);
      add(swatch);
    }
  }

  void highlight(Theme edited) {
    for (PresetSwatch s : swatches) {
      s.setSelected(s.preset().matches(edited));
    }
  }
}
