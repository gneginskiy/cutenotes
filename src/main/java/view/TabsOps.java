package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Tab;
import model.Theme;

final class TabsOps {

  private TabsOps() {}

  static List<Tab> snapshot(
      List<String> order, Map<String, TabState> stateById, NoteEditor defaultArea) {
    List<Tab> result = new ArrayList<>();
    for (String id : order) {
      TabState s = stateById.get(id);
      result.add(new Tab(id, s.name, s.area.markdown()));
    }
    String def = defaultArea.markdown();
    if (!def.isBlank()) {
      result.add(new Tab(TabsPane.DEFAULT_ID, "default", def));
    }
    return result;
  }

  static void applyTheme(Theme t, NoteEditor defaultArea, Map<String, TabState> stateById) {
    defaultArea.applyTheme(t);
    stateById.values().forEach(s -> s.area.applyTheme(t));
  }
}
