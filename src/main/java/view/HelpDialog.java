package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class HelpDialog extends JDialog {

  private static final float MIN_FONT = 9f;
  private static final double SCREEN_FILL = 0.92;
  private int row;

  HelpDialog(JFrame owner) {
    super(owner, "Keyboard Shortcuts", false);
    JPanel content = buildContent();
    setContentPane(content);
    Dialogs.bindEscape(this);
    pack();
    fitToScreen(content);
    WindowPlacement.centerOnOwner(this);
  }

  private void fitToScreen(JPanel content) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int maxH = (int) (screen.height * SCREEN_FILL);
    if (getHeight() <= maxH) {
      return;
    }
    float scale = (float) maxH / getHeight();
    scaleFonts(content, scale);
    pack();
  }

  private static void scaleFonts(Container c, float scale) {
    for (Component child : c.getComponents()) {
      Font f = child.getFont();
      if (f != null) {
        child.setFont(f.deriveFont(Math.max(MIN_FONT, f.getSize2D() * scale)));
      }
      if (child instanceof Container nested) {
        scaleFonts(nested, scale);
      }
    }
  }

  private JPanel buildContent() {
    boolean mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
    String cmd = mac ? "Cmd" : "Ctrl";
    String alt = mac ? "Option" : "Alt";
    JPanel grid = new JPanel(new GridBagLayout());
    grid.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
    addTitle(grid);
    addSection(grid, "Tabs", tabsRows(cmd, alt));
    addSection(grid, "Search & Editing", editingRows(cmd));
    addSection(grid, "Display", displayRows(cmd));
    addSection(grid, "Help", new String[][] {{"Show this dialog", "F1"}});
    return grid;
  }

  private static String[][] tabsRows(String cmd, String alt) {
    return new String[][] {
      {"New tab", cmd + "+T"},
      {"Close tab", cmd + "+W"},
      {"Reopen last closed", cmd + "+Shift+T"},
      {"Reopen closed tab\u2026", cmd + "+R"},
      {"Next tab", alt + "+Tab"},
      {"Rename tab", "Double-click or right-click"}
    };
  }

  private static String[][] editingRows(String cmd) {
    return new String[][] {
      {"Find in current tab", cmd + "+F"},
      {"Next / previous match", "Enter / Shift+Enter"},
      {"Cut line", cmd + "+X"},
      {"Copy line", cmd + "+C"},
      {"Duplicate line", cmd + "+D"},
      {"Undo / Redo", cmd + "+Z  /  " + cmd + "+Y"}
    };
  }

  private static String[][] displayRows(String cmd) {
    return new String[][] {
      {"Zoom in / out", cmd + "+Shift+=  /  " + cmd + "+Shift+-"},
      {"Toggle menu & tabs strip", "Esc"},
      {"Options\u2026", cmd + "+O"}
    };
  }

  private void addTitle(JPanel grid) {
    JLabel title = new JLabel("cuteNotes \u2014 Keyboard Shortcuts");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = row++;
    g.gridwidth = 2;
    g.anchor = GridBagConstraints.WEST;
    g.insets = new Insets(0, 0, 18, 0);
    grid.add(title, g);
  }

  private void addSection(JPanel grid, String name, String[][] rows) {
    JLabel header = new JLabel(name);
    header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
    header.setForeground(new Color(80, 80, 80));
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = row++;
    g.gridwidth = 2;
    g.anchor = GridBagConstraints.WEST;
    g.insets = new Insets(row == 2 ? 0 : 16, 0, 8, 0);
    grid.add(header, g);
    for (String[] r : rows) {
      addRow(grid, r[0], r[1]);
    }
  }

  private void addRow(JPanel grid, String action, String keys) {
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = row;
    g.weightx = 0;
    g.anchor = GridBagConstraints.WEST;
    g.insets = new Insets(4, 0, 4, 36);
    grid.add(new JLabel(action), g);
    g.gridx = 1;
    g.weightx = 1;
    g.insets = new Insets(4, 0, 4, 0);
    JLabel keysLabel = new JLabel(keys);
    keysLabel.setFont(keysLabel.getFont().deriveFont(Font.BOLD));
    grid.add(keysLabel, g);
    row++;
  }
}
