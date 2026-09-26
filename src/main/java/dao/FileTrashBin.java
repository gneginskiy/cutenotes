package dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import lombok.SneakyThrows;
import model.Tab;
import model.TabMeta;

/**
 * The bin as a folder ({@code <data>/trash}) of ordinary note files: the notes repository code is
 * reused for it, and a deleted note can be recovered by hand, too.
 */
public final class FileTrashBin implements TrashBin {

  static final String DIR = "trash";

  private final FileTabRepository notes;
  private final FileTabRepository bin;

  FileTrashBin(FileTabRepository notes, FileTabRepository bin) {
    this.notes = notes;
    this.bin = bin;
  }

  @Override
  public List<TabMeta> list() {
    return bin.listMeta();
  }

  @Override
  public Tab load(String id) {
    return bin.load(id);
  }

  @Override
  @SneakyThrows
  public void restore(String id) {
    Path file = bin.fileOf(id);
    if (file != null) {
      FileTabRepository.moveInto(file, notes.dir());
    }
  }

  @Override
  public void purge(String id) {
    bin.delete(id);
  }

  @Override
  @SneakyThrows
  public int purgeOlderThan(Instant cutoff) {
    int purged = 0;
    for (TabMeta meta : bin.listMeta()) {
      if (meta.lastModified().isBefore(cutoff)) {
        Path file = bin.fileOf(meta.id());
        if (file != null && Files.deleteIfExists(file)) {
          purged++;
        }
      }
    }
    return purged;
  }
}
