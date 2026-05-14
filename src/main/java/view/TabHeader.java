package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

class TabHeader extends JPanel {

  static Color ACTIVE = new Color(250, 250, 180);
  static Color INACTIVE = new Color(235, 235, 165);
  static Color ACTIVE_FG = Color.BLACK;
  static Color INACTIVE_FG = new Color(110, 110, 90);

  private final Map<String, JLabel> labelsById = new HashMap<>();
  private final Consumer<String> onSelect;
  private final BiConsumer<String, String> onRename;
  private final TabDrag drag;
  private String activeId;

  TabHeader(
      Consumer<String> onSelect,
      BiConsumer<String, String> onRename,
      Consumer<List<String>> onReorder) {
    this.onSelect = onSelect;
    this.onRename = onRename;
    this.drag = new TabDrag(this, labelsById, onReorder);
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    setBackground(INACTIVE);
  }

  void addTab(String id, String name) {
    JLabel label = buildLabel(id, name);
    labelsById.put(id, label);
    drag.attach(label);
    add(label);
    revalidate();
  }

  void removeTab(String id) {
    JLabel label = labelsById.remove(id);
    if (label != null) {
      remove(label);
      revalidate();
      repaint();
    }
  }

  void selectTab(String id) {
    this.activeId = id;
    labelsById.forEach(
        (i, l) -> {
          boolean sel = i.equals(id);
          l.setBackground(sel ? ACTIVE : INACTIVE);
          l.setForeground(sel ? ACTIVE_FG : INACTIVE_FG);
          l.setFont(l.getFont().deriveFont(sel ? Font.BOLD : Font.PLAIN));
        });
    repaint();
  }

  void applyTheme(Color bg, Color fg) {
    ACTIVE = bg;
    INACTIVE = Colors.contrast(bg, 0.12f);
    ACTIVE_FG = fg;
    INACTIVE_FG = Colors.blend(fg, INACTIVE, 0.35f);
    setBackground(INACTIVE);
    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.divider(bg)));
    selectTab(activeId);
  }

  boolean isEditing() {
    return getComponentCount() != labelsById.size();
  }

  private JLabel buildLabel(String id, String name) {
    JLabel label = new JLabel(name);
    label.setOpaque(true);
    label.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
              popup(e, id, label);
              return;
            }
            if (e.getButton() == MouseEvent.BUTTON1) {
              if (e.getClickCount() == 2) {
                startEdit(id, label);
              } else {
                onSelect.accept(id);
              }
            }
          }
        });
    return label;
  }

  private void popup(MouseEvent e, String id, JLabel label) {
    JPopupMenu menu = new JPopupMenu();
    menu.add("Rename").addActionListener(a -> startEdit(id, label));
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void startEdit(String id, JLabel label) {
    int idx = getComponentZOrder(label);
    JTextField field =
        InlineEditor.create(
            label.getText(),
            (f, text) -> finishEdit(id, label, f, text),
            f -> finishEdit(id, label, f, null));
    remove(label);
    add(field, idx);
    revalidate();
    repaint();
    field.requestFocusInWindow();
  }

  private void finishEdit(String id, JLabel label, JTextField field, String text) {
    int idx = getComponentZOrder(field);
    if (idx < 0) {
      return;
    }
    remove(field);
    add(label, idx);
    revalidate();
    repaint();
    if (text == null) {
      return;
    }
    String newName = text.trim();
    if (!newName.isBlank() && !newName.equals(label.getText())) {
      label.setText(newName);
      onRename.accept(id, newName);
    }
  }
}
