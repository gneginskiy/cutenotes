package dao;

import java.time.Instant;
import java.util.List;

import model.Tab;
import model.TabMeta;

/** "Recently deleted": deleted notes kept for a while, so a deletion can be undone. */
public interface TrashBin {

  /** A bin that keeps nothing (for repositories without one). */
  TrashBin NONE =
      new TrashBin() {
        @Override
        public List<TabMeta> list() {
          return List.of();
        }

        @Override
        public Tab load(String id) {
          return new Tab(id, "", "");
        }

        @Override
        public void restore(String id) {
          // nothing kept
        }

        @Override
        public void purge(String id) {
          // nothing kept
        }

        @Override
        public int purgeOlderThan(Instant cutoff) {
          return 0;
        }
      };

  /** The deleted notes; {@link TabMeta#lastModified()} is when each was deleted. */
  List<TabMeta> list();

  Tab load(String id);

  /** Moves a deleted note back among the notes. */
  void restore(String id);

  /** Deletes a note for good. */
  void purge(String id);

  /** Deletes for good every note deleted before {@code cutoff}; returns how many. */
  int purgeOlderThan(Instant cutoff);
}
