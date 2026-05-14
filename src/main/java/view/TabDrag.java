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
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

final class TabDrag {

  private static final int THRESHOLD = 5;

  private final Container header;
  private final Map<String, JLabel> labelsById;
  private final Consumer<List<String>> onReorder;
  private JLabel dragLabel;
  private int pressX;
  private boolean active;

  TabDrag(Container header, Map<String, JLabel> labelsById, Consumer<List<String>> onReorder) {
    this.header = header;
    this.labelsById = labelsById;
    this.onReorder = onReorder;
  }

  void attach(JLabel label) {
    label.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
              start(e, label);
            }
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            end();
          }
        });
    label.addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            drag(e, label);
          }
        });
  }

  private void start(MouseEvent e, JLabel label) {
    dragLabel = label;
    pressX = SwingUtilities.convertPoint(label, e.getPoint(), header).x;
    active = false;
  }

  private void drag(MouseEvent e, JLabel source) {
    if (dragLabel == null) {
      return;
    }
    int x = SwingUtilities.convertPoint(source, e.getPoint(), header).x;
    if (!active && Math.abs(x - pressX) < THRESHOLD) {
      return;
    }
    active = true;
    for (Component c : header.getComponents()) {
      if (c == dragLabel || !(c instanceof JLabel)) {
        continue;
      }
      Rectangle b = c.getBounds();
      if (x >= b.x && x <= b.x + b.width) {
        header.setComponentZOrder(dragLabel, header.getComponentZOrder(c));
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
    dragLabel = null;
    active = false;
  }

  private List<String> currentOrder() {
    List<String> ids = new ArrayList<>();
    for (Component c : header.getComponents()) {
      for (Map.Entry<String, JLabel> e : labelsById.entrySet()) {
        if (e.getValue() == c) {
          ids.add(e.getKey());
          break;
        }
      }
    }
    return ids;
  }
}
