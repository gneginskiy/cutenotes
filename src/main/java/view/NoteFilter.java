package view;

import java.util.Locale;
import java.util.Set;

import model.TabMeta;
import util.NoteIndex;

/**
 * Which notes the browser lists: optionally without the ones already open in tabs, and only those
 * matching the query — by name, by text (once the {@link NoteIndex} is built) or, for {@code #tag},
 * by tag.
 */
record NoteFilter(boolean showOpen, Set<String> openIds, String query, NoteIndex index) {

  NoteFilter {
    query = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
  }

  NoteFilter(boolean showOpen, Set<String> openIds, String query) {
    this(showOpen, openIds, query, null);
  }

  static NoteFilter all(Set<String> openIds) {
    return new NoteFilter(true, openIds, "", null);
  }

  boolean searching() {
    return !query.isEmpty();
  }

  boolean tagQuery() {
    return query.length() > 1 && query.startsWith("#");
  }

  boolean accepts(TabMeta note) {
    if (!showOpen && openIds.contains(note.id())) {
      return false;
    }
    if (!searching()) {
      return true;
    }
    if (tagQuery()) {
      return index != null && index.hasTag(note.id(), query.substring(1));
    }
    return nameMatches(note.name()) || (index != null && index.contains(note.id(), query));
  }

  boolean nameMatches(String name) {
    if (!searching()) {
      return true;
    }
    return !tagQuery() && name != null && name.toLowerCase(Locale.ROOT).contains(query);
  }

  /** The text around the match when a note is found by its text rather than its name. */
  String snippet(TabMeta note) {
    boolean byText = searching() && !tagQuery() && index != null && !nameMatches(note.name());
    return byText ? index.snippet(note.id(), query) : null;
  }

  NoteFilter withShowOpen(boolean value) {
    return new NoteFilter(value, openIds, query, index);
  }

  NoteFilter withQuery(String value) {
    return new NoteFilter(showOpen, openIds, value, index);
  }

  NoteFilter withIndex(NoteIndex value) {
    return new NoteFilter(showOpen, openIds, query, value);
  }
}
