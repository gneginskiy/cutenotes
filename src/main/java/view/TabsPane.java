package view;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.Tab;
import model.Theme;

public class TabsPane {

  private final JPanel root = new JPanel(new BorderLayout());
  private final JPanel inner = new JPanel(new BorderLayout());
  private final TabCards cards = new TabCards();
  private final OpenTabs tabs = new OpenTabs();
  private final TabStrip tabStrip =
      new TabStrip(
          this::select,
          (id, name) -> this.identity.renamedByUser(id, name),
          tabs::reorder,
          id -> this.identity.metas().get(id).color(),
          (id, color) -> this.identity.setColor(id, color),
          root);
  private final TabHeader header = tabStrip.header();
  private final TabIdentity identity = new TabIdentity(tabs, header);
  private final NoteEditor defaultArea = cards.defaultArea();

  public TabsPane() {
    root.add(tabStrip.component(), BorderLayout.NORTH);
    inner.add(cards.component(), BorderLayout.CENTER);
    root.add(inner, BorderLayout.CENTER);
  }

  public JPanel component() {
    return root;
  }

  /** The tabs bar: where the session plugs in its requests and the window its notifications. */
  TabStrip strip() {
    return tabStrip;
  }

  String activeId() {
    return tabs.activeId();
  }

  /** Names and colours of the tabs, and the per-note settings behind them. */
  TabIdentity identity() {
    return identity;
  }

  public void addBelowHeader(JComponent c) {
    inner.add(c, BorderLayout.NORTH);
    inner.revalidate();
  }

  public List<String> openIds() {
    return tabs.ids();
  }

  public void setDefaultContent(String text) {
    defaultArea.setMarkdown(text);
  }

  public List<Tab> snapshot() {
    return tabs.snapshot(defaultArea);
  }

  Tab snapshotOf(String id) {
    return tabs.snapshotOf(id, defaultArea);
  }

  public void openTab(String id, String name, String text) {
    if (tabs.contains(id)) {
      select(id);
      return;
    }
    NoteEditor area = new NoteEditor(text);
    JScrollPane pane = TabPanes.content(area);
    cards.add(pane, id);
    header.addTab(id, name);
    tabs.add(id, new TabState(name, area, pane));
    identity.opened(id, name, area);
    select(id);
  }

  /** Closes the tab with {@code id} (active or not); returns its final state, or null. */
  Tab close(String id) {
    boolean wasActive = id != null && id.equals(tabs.activeId());
    int idx = tabs.indexOf(id);
    identity.closing(id);
    TabState s = tabs.remove(id);
    if (s == null) {
      return null;
    }
    cards.remove(s.pane);
    header.removeTab(id);
    String neighbour = tabs.neighbourOf(idx);
    if (neighbour == null) {
      cards.showDefault();
      tabStrip.revealer().hide();
    } else if (wasActive) {
      select(neighbour);
    }
    return new Tab(id, s.name, s.area.markdown());
  }

  public String popLastClosed() {
    return tabs.popReopenable();
  }

  /** The next (1) or previous (-1) tab, wrapping around. */
  void selectRelative(int step) {
    select(step > 0 ? tabs.next() : tabs.previous());
  }

  /** Selects the tab at {@code index} (0-based); -1 means the last tab. */
  void selectPosition(int index) {
    select(tabs.atPosition(index));
  }

  public NoteEditor activeArea() {
    return areaOf(tabs.activeId());
  }

  /** The editor of a tab; the scratch area for an id that is not open. */
  NoteEditor areaOf(String id) {
    TabState s = id == null ? null : tabs.get(id);
    return s == null ? defaultArea : s.area;
  }

  public void applyTheme(Theme t) {
    defaultArea.applyTheme(t);
    tabs.applyTheme(t);
    tabStrip.applyTheme(UiPalette.of(t));
  }

  void select(String id) {
    if (!tabs.contains(id)) {
      return;
    }
    tabs.setActive(id);
    cards.show(id);
    header.selectTab(id);
    TabPanes.focusLater(tabs.get(id).area);
  }
}
