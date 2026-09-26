package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.NoteMeta;

class FileNoteMetaStoreTest {

  @Test
  void roundTripSkippingDefaults(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("meta.txt");
    FileNoteMetaStore store = new FileNoteMetaStore(file);
    Map<String, NoteMeta> all = new LinkedHashMap<>();
    all.put("a", NoteMeta.defaults("a").withPinned(true).withColor("red"));
    all.put("b", NoteMeta.defaults("b").withAutoTitle(true));
    all.put("c", NoteMeta.defaults("c"));

    store.write(all);

    Map<String, NoteMeta> back = store.read();
    assertEquals(2, back.size());
    assertEquals(all.get("a"), back.get("a"));
    assertEquals(all.get("b"), back.get("b"));
    assertTrue(Files.readString(file).contains("a\tp\tred"));
  }

  @Test
  void missingFileAndJunkLines(@TempDir Path tmp) throws Exception {
    assertTrue(new FileNoteMetaStore(tmp.resolve("none")).read().isEmpty());
    Path file = tmp.resolve("meta.txt");
    Files.writeString(file, "junk\n\tp\tred\nx\ta\t\n");
    Map<String, NoteMeta> back = new FileNoteMetaStore(file).read();
    assertEquals(1, back.size());
    assertEquals(NoteMeta.defaults("x").withAutoTitle(true), back.get("x"));
    assertEquals("", NoteMeta.defaults("y").withColor(null).color());
  }
}
