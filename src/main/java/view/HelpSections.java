package view;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** One column of the {@link HelpDialog}: section captions followed by action / key-cap rows. */
final class HelpSections {

  private final JPanel panel = new JPanel(new GridBagLayout());
  private final UiPalette palette;
  private final boolean mac;
  private int row;

  HelpSections(UiPalette palette, boolean mac) {
    this.palette = palette;
    this.mac = mac;
    panel.setOpaque(false);
  }

  JPanel panel() {
    return panel;
  }

  HelpSections section(String name, String[][] rows) {
    JLabel caption = new JLabel(name.toUpperCase(Locale.ROOT));
    caption.setFont(UiFonts.ui(Font.BOLD, 11f));
    caption.setForeground(palette.accent());
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = row++;
    g.gridwidth = 2;
    g.anchor = GridBagConstraints.WEST;
    g.insets = new Insets(row == 1 ? 0 : 22, 0, 8, 0);
    panel.add(caption, g);
    for (String[] r : rows) {
      addRow(r[0], r[1]);
    }
    return this;
  }

  /** Pushes the content to the top when the neighbouring column is taller. */
  HelpSections end() {
    GridBagConstraints g = new GridBagConstraints();
    g.gridy = row++;
    g.weighty = 1;
    panel.add(new JLabel(), g);
    return this;
  }

  private void addRow(String action, String keys) {
    JLabel label = new JLabel(action);
    label.setFont(UiFonts.ui(Font.PLAIN, 13f));
    label.setForeground(palette.fg());
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = row;
    g.weightx = 1;
    g.anchor = GridBagConstraints.WEST;
    g.insets = new Insets(3, 0, 3, 24);
    panel.add(label, g);
    g.gridx = 1;
    g.weightx = 0;
    g.anchor = GridBagConstraints.EAST;
    g.insets = new Insets(3, 0, 3, 0);
    panel.add(new KeyCaps(ShortcutText.parse(keys, mac), palette), g);
    row++;
  }
}
