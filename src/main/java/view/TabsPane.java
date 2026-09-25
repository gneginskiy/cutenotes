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
  private final TabStrip tabStrip = new TabStrip(this::select, tabs::rename, tabs::reorder, root);
  private final TabHeader header = tabStrip.header();
  private final TabHistory history = new TabHistory();
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
    select(id);
  }

  public Tab closeCurrent() {
    return close(tabs.activeId());
  }

  /** Closes the tab with {@code id} (active or not); returns its final state, or null. */
  Tab close(String id) {
    boolean wasActive = id != null && id.equals(tabs.activeId());
    int idx = tabs.indexOf(id);
    TabState s = tabs.remove(id);
    if (s == null) {
      return null;
    }
    history.record(id);
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
    return history.popReopenable(tabs.ids());
  }

  public void selectNext() {
    select(tabs.next());
  }

  public NoteEditor activeArea() {
    TabState s = tabs.get(tabs.activeId());
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
