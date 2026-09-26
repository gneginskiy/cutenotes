package util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.TabMeta;

/** Remembers the modification time of each open note, to notice files changed since last look. */
public final class ModTimes {

  private final Map<String, Instant> seen = new HashMap<>();

  /**
   * The open notes whose file changed since the previous call. A note seen for the first time only
   * starts being watched.
   */
  public synchronized List<String> changed(List<TabMeta> notes, Collection<String> open) {
    List<String> changed = new ArrayList<>();
    Map<String, Instant> now = new HashMap<>();
    for (TabMeta note : notes) {
      if (!open.contains(note.id())) {
        continue;
      }
      now.put(note.id(), note.lastModified());
      Instant before = seen.get(note.id());
      if (before != null && !before.equals(note.lastModified())) {
        changed.add(note.id());
      }
    }
    seen.clear();
    seen.putAll(now);
    return changed;
  }
}
