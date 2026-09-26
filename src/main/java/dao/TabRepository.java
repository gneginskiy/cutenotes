package dao;

import java.util.List;

import model.Tab;
import model.TabMeta;

public interface TabRepository {

  List<TabMeta> listMeta();

  Tab load(String id);

  void save(String id, String name, String content);

  /** Deletes a note for good (used for notes left empty). */
  void delete(String id);

  String newId();

  /** Moves a note to "Recently deleted"; a repository without a bin deletes it. */
  default void trash(String id) {
    delete(id);
  }

  default TrashBin trashBin() {
    return TrashBin.NONE;
  }

  /** Earlier versions of the notes; a repository without history keeps none. */
  default NoteHistory history() {
    return NoteHistory.NONE;
  }
}
