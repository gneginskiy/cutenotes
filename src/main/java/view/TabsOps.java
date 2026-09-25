package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Tab;
import model.Theme;

final class TabsOps {

  private static final String DEFAULT_NAME = "default";

  private TabsOps() {}

  static List<Tab> snapshot(
      List<String> order, Map<String, TabState> stateById, NoteEditor defaultArea) {
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

  static Tab snapshotOf(String id, Map<String, TabState> stateById, NoteEditor defaultArea) {
    if (Tab.DEFAULT_ID.equals(id)) {
      return new Tab(id, DEFAULT_NAME, defaultArea.markdown());
    }
    TabState s = stateById.get(id);
    return s == null ? null : new Tab(id, s.name, s.area.markdown());
  }

  static void applyTheme(Theme t, NoteEditor defaultArea, Map<String, TabState> stateById) {
    defaultArea.applyTheme(t);
    stateById.values().forEach(s -> s.area.applyTheme(t));
  }
}
