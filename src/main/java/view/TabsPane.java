package view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.Tab;
import model.Theme;

public class TabsPane {

  private final JPanel root = new JPanel(new BorderLayout());
  private final JPanel inner = new JPanel(new BorderLayout());
  private final TabCards cards = new TabCards();
  private final List<String> order = new ArrayList<>();
  private final Map<String, TabState> stateById = new HashMap<>();
  private final TabHeader header =
      new TabHeader(this::select, (id, name) -> stateById.get(id).name = name, this::reorder);
  private final JScrollPane headerScroll = TabPanes.header(header);
  private final TabHistory history = new TabHistory();
  private final NoteEditor defaultArea = cards.defaultArea();
  private final TabStrip tabStrip = new TabStrip(headerScroll, root, header::isEditing);
  private String activeId;

  public TabsPane() {
    root.add(tabStrip.component(), BorderLayout.NORTH);
    inner.add(cards.component(), BorderLayout.CENTER);
    root.add(inner, BorderLayout.CENTER);
  }

  public JPanel component() {
    return root;
  }

  public void addBelowHeader(JComponent c) {
    inner.add(c, BorderLayout.NORTH);
    inner.revalidate();
  }

  public Revealer revealer() {
    return tabStrip.revealer();
  }

  public boolean pinned() {
    return tabStrip.pinned();
  }

  public List<String> openIds() {
    return new ArrayList<>(order);
  }

  public void setDefaultContent(String text) {
    defaultArea.setMarkdown(text);
  }

  public List<Tab> snapshot() {
    return TabsOps.snapshot(order, stateById, defaultArea);
  }

  Tab snapshotOf(String id) {
    return TabsOps.snapshotOf(id, stateById, defaultArea);
  }

  public void openTab(String id, String name, String text) {
    if (stateById.containsKey(id)) {
      select(id);
      return;
    }
    NoteEditor area = new NoteEditor(text);
    JScrollPane pane = TabPanes.content(area);
    cards.add(pane, id);
    header.addTab(id, name);
    order.add(id);
    stateById.put(id, new TabState(name, area, pane));
    select(id);
  }

  public Tab closeCurrent() {
    return close(activeId);
  }

  /** Closes the tab with {@code id} (active or not); returns its final state, or null. */
  Tab close(String id) {
    TabState s = id == null ? null : stateById.remove(id);
    if (s == null) {
      return null;
    }
    int idx = order.indexOf(id);
    history.record(id);
    order.remove(id);
    cards.remove(s.pane);
    header.removeTab(id);
    if (id.equals(activeId)) {
      activeId = null;
      if (order.isEmpty()) {
        cards.showDefault();
      } else {
        select(order.get(Math.max(0, idx - 1)));
      }
    }
    return new Tab(id, s.name, s.area.markdown());
  }

  public String popLastClosed() {
    return history.popReopenable(order);
  }

  public void selectNext() {
    if (order.size() >= 2) {
      select(order.get((order.indexOf(activeId) + 1) % order.size()));
    }
  }

  public NoteEditor activeArea() {
    TabState s = stateById.get(activeId);
    return s == null ? defaultArea : s.area;
  }

  public void applyTheme(Theme t) {
    TabsOps.applyTheme(t, defaultArea, stateById);
    header.applyTheme(t.bg(), t.fg());
    tabStrip.applyTheme(t.bg(), t.fg());
  }

  void select(String id) {
    if (id == null || !stateById.containsKey(id)) {
      return;
    }
    activeId = id;
    cards.show(id);
    header.selectTab(id);
    TabPanes.focusLater(stateById.get(id).area);
  }

  private void reorder(List<String> ids) {
    order.clear();
    order.addAll(ids);
  }
}
