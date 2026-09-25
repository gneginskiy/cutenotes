package view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

final class TabDrag {

  private static final int THRESHOLD = 5;

  private final Container header;
  private final Map<String, TabChip> chipsById;
  private final Consumer<List<String>> onReorder;
  private TabChip dragChip;
  private int pressX;
  private boolean active;

  TabDrag(Container header, Map<String, TabChip> chipsById, Consumer<List<String>> onReorder) {
    this.header = header;
    this.chipsById = chipsById;
    this.onReorder = onReorder;
  }

  void attach(TabChip chip) {
    chip.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
              start(e, chip);
            }
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            end();
          }
        });
    chip.addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            drag(e, chip);
          }
        });
  }

  private void start(MouseEvent e, TabChip chip) {
    dragChip = chip;
    pressX = SwingUtilities.convertPoint(chip, e.getPoint(), header).x;
    active = false;
  }

  private void drag(MouseEvent e, TabChip source) {
    if (dragChip == null) {
      return;
    }
    int x = SwingUtilities.convertPoint(source, e.getPoint(), header).x;
    if (!active && Math.abs(x - pressX) < THRESHOLD) {
      return;
    }
    active = true;
    for (Component c : header.getComponents()) {
      if (c == dragChip || !(c instanceof TabChip)) {
        continue;
      }
      Rectangle b = c.getBounds();
      if (x >= b.x && x <= b.x + b.width) {
        header.setComponentZOrder(dragChip, header.getComponentZOrder(c));
        header.revalidate();
        header.repaint();
        break;
      }
    }
  }

  private void end() {
    if (active) {
      onReorder.accept(currentOrder());
    }
    dragChip = null;
    active = false;
  }

  private List<String> currentOrder() {
    List<String> ids = new ArrayList<>();
    for (Component c : header.getComponents()) {
      for (Map.Entry<String, TabChip> e : chipsById.entrySet()) {
        if (e.getValue() == c) {
          ids.add(e.getKey());
          break;
        }
      }
    }
    return ids;
  }
}
