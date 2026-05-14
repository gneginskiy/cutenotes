package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import model.Tab;
import model.Theme;

public class TabsPane {

  public static final String DEFAULT_ID = "__default__";

  private final JPanel root = new JPanel(new BorderLayout());
  private final JPanel inner = new JPanel(new BorderLayout());
  private final JPanel content = new JPanel();
  private final CardLayout cards = new CardLayout();
  private final List<String> order = new ArrayList<>();
  private final Map<String, TabState> stateById = new HashMap<>();
  private final TabHeader header =
      new TabHeader(this::select, (id, name) -> stateById.get(id).name = name, this::reorder);
  private final JScrollPane headerScroll = TabPanes.header(header);
  private final TabHistory history = new TabHistory();
  private final JTextArea defaultArea = TextAreaFactory.create("");
  private final JScrollPane defaultPane = TabPanes.content(defaultArea);
  private final Revealer tabsRevealer = new Revealer(headerScroll, root, header::isEditing);
  private String activeId;

  public TabsPane() {
    content.setLayout(cards);
    content.setBackground(ThemeHolder.current().bg());
    headerScroll.addMouseWheelListener(
        e -> {
          var b = headerScroll.getHorizontalScrollBar();
          b.setValue(b.getValue() + e.getUnitsToScroll() * b.getUnitIncrement());
        });
    root.add(headerScroll, BorderLayout.NORTH);
    inner.add(content, BorderLayout.CENTER);
    root.add(inner, BorderLayout.CENTER);
    content.add(defaultPane, DEFAULT_ID);
    cards.show(content, DEFAULT_ID);
  }

  public JPanel component() {
    return root;
  }

  public void addBelowHeader(JComponent c) {
    inner.add(c, BorderLayout.NORTH);
    inner.revalidate();
  }

  public Revealer revealer() {
    return tabsRevealer;
  }

  public List<String> openIds() {
    return new ArrayList<>(order);
  }

  public void setDefaultContent(String text) {
    defaultArea.setText(text.replace("\n", "\r\n"));
  }

  public List<Tab> snapshot() {
    return TabsOps.snapshot(order, stateById, defaultArea);
  }

  public void openTab(String id, String name, String text) {
    if (stateById.containsKey(id)) {
      select(id);
      return;
    }
    JTextArea area = TextAreaFactory.create(text.replace("\n", "\r\n"));
    JScrollPane pane = TabPanes.content(area);
    content.add(pane, id);
    header.addTab(id, name);
    order.add(id);
    stateById.put(id, new TabState(name, area, pane));
    select(id);
  }

  public Tab closeCurrent() {
    if (activeId == null) {
      return null;
    }
    String closing = activeId;
    int idx = order.indexOf(closing);
    history.record(closing);
    TabState s = stateById.remove(closing);
    order.remove(closing);
    content.remove(s.pane);
    header.removeTab(closing);
    activeId = null;
    if (order.isEmpty()) {
      cards.show(content, DEFAULT_ID);
      focusLater(defaultArea);
    } else {
      select(order.get(Math.max(0, idx - 1)));
    }
    return new Tab(closing, s.name, s.area.getText().replace("\r\n", "\n"));
  }

  public String popLastClosed() {
    return history.popReopenable(order);
  }

  public void selectNext() {
    if (order.size() >= 2) {
      select(order.get((order.indexOf(activeId) + 1) % order.size()));
    }
  }

  public JTextArea activeArea() {
    TabState s = stateById.get(activeId);
    return s == null ? defaultArea : s.area;
  }

  public void applyTheme(Theme t) {
    TabsOps.applyTheme(t, defaultArea, stateById);
    header.applyTheme(t.bg());
  }

  private void select(String id) {
    if (id == null || !stateById.containsKey(id)) {
      return;
    }
    activeId = id;
    cards.show(content, id);
    header.selectTab(id);
    focusLater(stateById.get(id).area);
  }

  private static void focusLater(JTextArea area) {
    SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(area::requestFocusInWindow));
  }

  private void reorder(List<String> ids) {
    order.clear();
    order.addAll(ids);
  }
}
