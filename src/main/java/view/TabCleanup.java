package view;

import java.util.ArrayList;
import java.util.List;

import dao.TabRepository;
import model.Tab;

final class TabCleanup {

  private TabCleanup() {}

  static List<String> pruneEmpty(List<Tab> tabs, TabRepository repo) {
    List<String> kept = new ArrayList<>();
    for (Tab tab : tabs) {
      if (TabsPane.DEFAULT_ID.equals(tab.id())) {
        continue;
      }
      if (tab.content().isBlank()) {
        repo.delete(tab.id());
      } else {
        kept.add(tab.id());
      }
    }
    return kept;
  }
}
