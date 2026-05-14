package view;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

final class ColorChoosers {

  private ColorChoosers() {}

  static void compact(JColorChooser chooser) {
    for (AbstractColorChooserPanel p : chooser.getChooserPanels()) {
      if (p.getDisplayName().equalsIgnoreCase("HSV")) {
        chooser.setChooserPanels(new AbstractColorChooserPanel[] {p});
        break;
      }
    }
    chooser.setPreviewPanel(new JPanel());
  }
}
