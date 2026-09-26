package dao;

import java.time.Instant;
import java.util.List;

/** Earlier versions of notes (see {@link HistoryStore}). */
public interface NoteHistory {

  /** A history that keeps nothing (for repositories without one). */
  NoteHistory NONE =
      new NoteHistory() {
        @Override
        public void snapshot(String id, String content, Instant now, boolean force) {
          // nothing kept
        }

        @Override
        public List<Instant> versions(String id) {
          return List.of();
        }

        @Override
        public String load(String id, Instant version) {
          return "";
        }

        @Override
        public void deleteAll(String id) {
          // nothing kept
        }
      };

  /** Records {@code content} unless a version was recorded recently ({@code force}: anyway). */
  void snapshot(String id, String content, Instant now, boolean force);

  /** When each kept version was recorded, newest first. */
  List<Instant> versions(String id);

  /** The text of one version; empty if it is gone. */
  String load(String id, Instant version);

  /** Forgets every version of a note. */
  void deleteAll(String id);
}
