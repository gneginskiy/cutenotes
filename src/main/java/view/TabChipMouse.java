package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/**
 * Mouse gestures on a tab: click selects, double-click renames, middle-click closes, right-click
 * opens the tab menu.
 */
final class TabChipMouse extends MouseAdapter {

  private final TabHeader header;
  private final String id;
  private final TabChip chip;

  TabChipMouse(TabHeader header, String id, TabChip chip) {
    this.header = header;
    this.id = id;
    this.chip = chip;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      showMenu(e);
    } else if (SwingUtilities.isMiddleMouseButton(e)) {
      header.actions().close().accept(id);
    } else if (SwingUtilities.isLeftMouseButton(e)) {
      if (e.getClickCount() == 2) {
        header.startRename(id);
      } else {
        header.actions().select().accept(id);
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      showMenu(e);
    }
  }

  private void showMenu(MouseEvent e) {
    TabHeader.Actions actions = header.actions();
    TabMenu.show(
        chip,
        e.getX(),
        e.getY(),
        header.palette(),
        new TabMenu.Actions(
            () -> header.startRename(id),
            () -> actions.close().accept(id),
            () -> actions.closeOthers().accept(id),
            header.tabCount() > 1,
            actions.colorOf().apply(id),
            color -> actions.setColor().accept(id, color)));
  }
}
