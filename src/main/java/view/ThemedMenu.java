package view;

import javax.swing.JMenu;
import javax.swing.plaf.basic.BasicMenuUI;

final class ThemedMenu extends JMenu {

  ThemedMenu(String label) {
    super(label);
    setUI(new BasicMenuUI());
    setOpaque(true);
  }
}
