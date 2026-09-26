package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Tab;
import model.Theme;

/** Bookkeeping of the open tabs: their order in the bar, their editors and which one is active. */
final class OpenTabs {

  private static final String DEFAULT_NAME = "default";

  private final List<String> order = new ArrayList<>();
  private final Map<String, TabState> stateById = new HashMap<>();
  private final TabHistory closed = new TabHistory();
  private String activeId;

  boolean contains(String id) {
    return id != null && stateById.containsKey(id);
  }

  TabState get(String id) {
    return stateById.get(id);
  }

  void add(String id, TabState state) {
    order.add(id);
    stateById.put(id, state);
  }

  /** Forgets {@code id} (remembering it as recently closed); returns its state, or null. */
  TabState remove(String id) {
    TabState state = id == null ? null : stateById.remove(id);
    if (state != null) {
      closed.record(id);
      order.remove(id);
      if (id.equals(activeId)) {
        activeId = null;
      }
    }
    return state;
  }

  /** The tab to show after closing the one at {@code closedIndex}: its left neighbour. */
  String neighbourOf(int closedIndex) {
    if (order.isEmpty()) {
      return null;
    }
    return order.get(Math.max(0, Math.min(order.size() - 1, closedIndex - 1)));
  }

  /** The most recently closed tab that is not open again; null when there is none. */
  String popReopenable() {
    return closed.popReopenable(order);
  }

  int indexOf(String id) {
    return order.indexOf(id);
  }

  List<String> ids() {
    return new ArrayList<>(order);
  }

  String activeId() {
    return activeId;
  }

  void setActive(String id) {
    activeId = id;
  }

  /** The tab after the active one, wrapping around; null with fewer than two tabs. */
  String next() {
    return order.size() < 2 ? null : order.get((order.indexOf(activeId) + 1) % order.size());
  }

  /** The tab before the active one, wrapping around; null with fewer than two tabs. */
  String previous() {
    int n = order.size();
    return n < 2 ? null : order.get((order.indexOf(activeId) - 1 + n) % n);
  }

  /** The tab at {@code index}; -1 (or beyond the end) means the last one; null without tabs. */
  String atPosition(int index) {
    if (order.isEmpty()) {
      return null;
    }
    return order.get(index < 0 || index >= order.size() ? order.size() - 1 : index);
  }

  void reorder(List<String> ids) {
    order.clear();
    order.addAll(ids);
  }

  void rename(String id, String name) {
    stateById.get(id).name = name;
  }

  List<Tab> snapshot(NoteEditor defaultArea) {
    List<Tab> result = new ArrayList<>();
    for (String id : order) {
      TabState s = stateById.get(id);
      result.add(new Tab(id, s.name, s.area.markdown()));
    }
    // Always included, even when blank: clearing the scratch area must reach the disk too,
    // otherwise the deleted text comes back on the next start.
    result.add(new Tab(Tab.DEFAULT_ID, DEFAULT_NAME, defaultArea.markdown()));
    return result;
  }

  Tab snapshotOf(String id, NoteEditor defaultArea) {
    if (Tab.DEFAULT_ID.equals(id)) {
      return new Tab(id, DEFAULT_NAME, defaultArea.markdown());
    }
    TabState s = stateById.get(id);
    return s == null ? null : new Tab(id, s.name, s.area.markdown());
  }

  void applyTheme(Theme t) {
    stateById.values().forEach(s -> s.area.applyTheme(t));
  }
}
