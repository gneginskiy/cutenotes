package view;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** The row of {@link TabChip}s: select, rename, close, drag to reorder. */
class TabHeader extends JPanel {

  /** What the header asks its owner to do. */
  record Actions(
      Consumer<String> select,
      BiConsumer<String, String> rename,
      Consumer<List<String>> reorder,
      Consumer<String> close,
      Consumer<String> closeOthers,
      Runnable newTab) {}

  private final Map<String, TabChip> chipsById = new LinkedHashMap<>();
  private final Actions actions;
  private final TabDrag drag;
  private transient UiPalette palette = UiPalette.current();
  private String activeId;

  TabHeader(Actions actions) {
    this.actions = actions;
    this.drag = new TabDrag(this, chipsById, actions.reorder());
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    setOpaque(true);
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
              actions.newTab().run();
            }
          }
        });
  }

  void addTab(String id, String name) {
    TabChip chip = new TabChip(name, () -> actions.close().accept(id));
    chip.applyPalette(palette);
    chip.addMouseListener(new TabChipMouse(this, id, chip));
    chipsById.put(id, chip);
    drag.attach(chip);
    add(chip);
    revalidate();
    repaint();
  }

  void removeTab(String id) {
    TabChip chip = chipsById.remove(id);
    if (chip != null) {
      remove(chip);
      revalidate();
      repaint();
    }
  }

  void selectTab(String id) {
    this.activeId = id;
    chipsById.forEach((i, chip) -> chip.setActive(i.equals(id)));
    TabChip active = chipsById.get(id);
    if (active != null) {
      SwingUtilities.invokeLater(() -> active.scrollRectToVisible(new Rectangle(active.getSize())));
    }
  }

  void applyTheme(UiPalette p) {
    this.palette = p;
    setBackground(p.chrome());
    chipsById.values().forEach(chip -> chip.applyPalette(p));
    selectTab(activeId);
  }

  boolean isEditing() {
    return chipsById.values().stream().anyMatch(TabChip::isEditing);
  }

  Actions actions() {
    return actions;
  }

  UiPalette palette() {
    return palette;
  }

  int tabCount() {
    return chipsById.size();
  }

  String titleOf(String id) {
    TabChip chip = chipsById.get(id);
    return chip == null ? null : chip.title();
  }

  void startRename(String id) {
    TabChip chip = chipsById.get(id);
    if (chip != null) {
      chip.startEdit(text -> finishRename(id, chip, text));
    }
  }

  private void finishRename(String id, TabChip chip, String text) {
    String name = text == null ? "" : text.trim();
    if (!name.isEmpty() && !name.equals(chip.title())) {
      chip.setTitle(name);
      actions.rename().accept(id, name);
    }
  }

  /** A hairline under the bar; the active tab paints over it and merges with the editor. */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(palette.border());
    g.fillRect(0, getHeight() - 1, getWidth(), 1);
  }
}
