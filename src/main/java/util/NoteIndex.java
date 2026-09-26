package util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import dao.TabRepository;
import markdown.MarkdownText;
import markdown.Tags;
import model.Tab;
import model.TabMeta;

/**
 * The text and tags of every note, for "find in all notes". Password-protected notes are indexed by
 * name only: their text is never decrypted for searching.
 */
public final class NoteIndex {

  static final int SNIPPET_RADIUS = 28;

  /** A note's searchable text, lower-cased text, tags and whether it is locked. */
  record Entry(String text, String lower, Set<String> tags, boolean locked) {}

  private final Map<String, Entry> entries;

  private NoteIndex(Map<String, Entry> entries) {
    this.entries = entries;
  }

  /** Reads every note of {@code repo} (skipping the scratch area). */
  public static NoteIndex build(TabRepository repo) {
    Map<String, String> contents = new LinkedHashMap<>();
    for (TabMeta meta : repo.listMeta()) {
      if (!Tab.DEFAULT_ID.equals(meta.id())) {
        contents.put(meta.id(), repo.load(meta.id()).content());
      }
    }
    return of(contents);
  }

  /** An index over note contents (Markdown) by id. */
  public static NoteIndex of(Map<String, String> contents) {
    Map<String, Entry> entries = new LinkedHashMap<>();
    contents.forEach(
        (id, content) -> {
          boolean locked = NoteCrypto.isEncrypted(content);
          String text = locked ? "" : MarkdownText.plain(content);
          entries.put(id, new Entry(text, text.toLowerCase(Locale.ROOT), Tags.of(text), locked));
        });
    return new NoteIndex(entries);
  }

  /** Whether the note's text contains {@code query} (case-insensitive). */
  public boolean contains(String id, String query) {
    Entry e = entries.get(id);
    return e != null && !query.isEmpty() && e.lower().contains(query.toLowerCase(Locale.ROOT));
  }

  public boolean hasTag(String id, String tag) {
    Entry e = entries.get(id);
    return e != null && e.tags().contains(tag.toLowerCase(Locale.ROOT));
  }

  public boolean locked(String id) {
    Entry e = entries.get(id);
    return e != null && e.locked();
  }

  /** One line of text around the first match: {@code …before match after…}; null if none. */
  public String snippet(String id, String query) {
    Entry e = entries.get(id);
    int at = e == null || query.isEmpty() ? -1 : e.lower().indexOf(query.toLowerCase(Locale.ROOT));
    if (at < 0) {
      return null;
    }
    int from = Math.max(0, at - SNIPPET_RADIUS);
    int to = Math.min(e.text().length(), at + query.length() + SNIPPET_RADIUS);
    String piece = e.text().substring(from, to).replaceAll("\\s+", " ").strip();
    return (from > 0 ? "…" : "") + piece + (to < e.text().length() ? "…" : "");
  }

  /** Every tag with the number of notes carrying it, alphabetically. */
  public Map<String, Integer> tagCounts() {
    Map<String, Integer> counts = new TreeMap<>();
    for (Entry e : entries.values()) {
      e.tags().forEach(tag -> counts.merge(tag, 1, Integer::sum));
    }
    return counts;
  }
}
