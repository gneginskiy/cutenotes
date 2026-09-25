package view;

import java.util.Locale;
import java.util.Set;

import model.TabMeta;

/**
 * Which notes the browser lists: optionally without the ones already open in tabs, and only those
 * whose name contains the typed query (case-insensitive).
 */
record NoteFilter(boolean showOpen, Set<String> openIds, String query) {

  NoteFilter {
    query = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
  }

  static NoteFilter all(Set<String> openIds) {
    return new NoteFilter(true, openIds, "");
  }

  boolean searching() {
    return !query.isEmpty();
  }

  boolean accepts(TabMeta note) {
    if (!showOpen && openIds.contains(note.id())) {
      return false;
    }
    return nameMatches(note.name());
  }

  boolean nameMatches(String name) {
    return !searching() || (name != null && name.toLowerCase(Locale.ROOT).contains(query));
  }

  NoteFilter withShowOpen(boolean value) {
    return new NoteFilter(value, openIds, query);
  }

  NoteFilter withQuery(String value) {
    return new NoteFilter(showOpen, openIds, value);
  }
}
