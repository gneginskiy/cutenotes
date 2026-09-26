package util;

import java.nio.file.Path;
import java.time.Instant;

import dao.FileTabRepository;
import dao.HistoryRepository;
import dao.HistoryStore;

/**
 * The notes of a data folder as the app uses them: files, recorded in the version history,
 * encrypted where protected (outermost, so neither the history nor the disk sees protected text).
 */
public record NoteStorage(EncryptingRepository repo, HistoryStore history, Path dataDir) {

  public static NoteStorage open(Path dataDir) {
    HistoryStore history = new HistoryStore(dataDir);
    HistoryRepository recorded = new HistoryRepository(new FileTabRepository(dataDir), history);
    return new NoteStorage(new EncryptingRepository(recorded, new Keyring()), history, dataDir);
  }

  /** Runs the startup {@link Housekeeping} on a virtual thread. */
  public Thread housekeepInBackground() {
    return Thread.ofVirtual()
        .name("housekeeping")
        .start(() -> Housekeeping.run(dataDir, repo, history, Instant.now()));
  }
}
