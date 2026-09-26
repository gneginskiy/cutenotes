package view;

import static view.Messages.tr;

import java.util.function.Consumer;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/** The "Colour" submenu of tabs and notes: no colour plus the {@link NoteColors}. */
final class ColorMenu {

  private ColorMenu() {}

  static JMenu build(String current, Consumer<String> choose) {
    JMenu menu = new JMenu(tr("tab.color"));
    ButtonGroup group = new ButtonGroup();
    add(menu, group, tr("tab.color.none"), "", current, choose);
    for (String name : NoteColors.names()) {
      add(menu, group, tr("tab.color." + name), name, current, choose);
    }
    return menu;
  }

  private static void add(
      JMenu menu,
      ButtonGroup group,
      String label,
      String value,
      String current,
      Consumer<String> choose) {
    JRadioButtonMenuItem item = new JRadioButtonMenuItem(label, NoteColors.dot(value, 9));
    item.setSelected(value.equals(current == null ? "" : current));
    item.addActionListener(e -> choose.accept(value));
    group.add(item);
    menu.add(item);
  }
}
