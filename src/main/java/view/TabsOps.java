package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JTextArea;

import model.Tab;
import model.Theme;

final class TabsOps {

  private TabsOps() {}

  static List<Tab> snapshot(
      List<String> order, Map<String, TabState> stateById, JTextArea defaultArea) {
    List<Tab> result = new ArrayList<>();
    for (String id : order) {
      TabState s = stateById.get(id);
      result.add(new Tab(id, s.name, s.area.getText().replace("\r\n", "\n")));
    }
    String def = defaultArea.getText().replace("\r\n", "\n");
    if (!def.isBlank()) {
      result.add(new Tab(TabsPane.DEFAULT_ID, "default", def));
    }
    return result;
  }

  static void applyTheme(Theme t, JTextArea defaultArea, Map<String, TabState> stateById) {
    TextAreaFactory.applyTheme(defaultArea, t);
    stateById.values().forEach(s -> TextAreaFactory.applyTheme(s.area, t));
  }
}
