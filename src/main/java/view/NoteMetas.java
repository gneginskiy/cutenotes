package view;

import java.util.LinkedHashMap;
import java.util.Map;

import dao.NoteMetaStore;
import model.NoteMeta;

/** The per-note settings in memory, written through to their store on every change. */
final class NoteMetas {

  private final NoteMetaStore store;
  private final Map<String, NoteMeta> all;

  NoteMetas(NoteMetaStore store) {
    this.store = store;
    this.all = new LinkedHashMap<>(store.read());
  }

  /** Keeps everything in memory only (tests, previews). */
  static NoteMetas inMemory() {
    Map<String, NoteMeta> map = new LinkedHashMap<>();
    return new NoteMetas(
        new NoteMetaStore() {
          @Override
          public Map<String, NoteMeta> read() {
            return map;
          }

          @Override
          public void write(Map<String, NoteMeta> all) {
            map.clear();
            map.putAll(all);
          }
        });
  }

  NoteMeta get(String id) {
    return all.getOrDefault(id, NoteMeta.defaults(id));
  }

  void put(NoteMeta meta) {
    if (meta.equals(get(meta.id()))) {
      return;
    }
    if (meta.isDefault()) {
      all.remove(meta.id());
    } else {
      all.put(meta.id(), meta);
    }
    store.write(all);
  }

  void remove(String id) {
    if (all.remove(id) != null) {
      store.write(all);
    }
  }
}
