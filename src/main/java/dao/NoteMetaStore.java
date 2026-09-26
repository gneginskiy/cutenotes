package dao;

import java.util.Map;

import model.NoteMeta;

public interface NoteMetaStore {

  /** Every stored entry by note id; notes without an entry use {@link NoteMeta#defaults}. */
  Map<String, NoteMeta> read();

  void write(Map<String, NoteMeta> all);
}
