package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class KeyValueFileTest {

  @Test
  void writesAndReadsBackInOrderSkippingNulls(@TempDir Path tmp) {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("b", "2");
    m.put("a", "x = y");
    m.put("skip", null);
    Path file = tmp.resolve("kv.txt");

    KeyValueFile.write(file, m);

    Map<String, String> back = KeyValueFile.read(file);
    assertEquals(Map.of("b", "2", "a", "x = y"), back);
    assertEquals("b", back.keySet().iterator().next());
  }

  @Test
  void missingFileIsEmptyAndJunkLinesAreIgnored(@TempDir Path tmp) {
    assertTrue(KeyValueFile.read(tmp.resolve("none")).isEmpty());
    assertEquals(Map.of("k", "v"), KeyValueFile.parse("junk\n=novalue\n k = v \n"));
  }

  @Test
  void numbersFallBack() {
    assertEquals(7, KeyValueFile.intOr(" 7 ", 1));
    assertEquals(1, KeyValueFile.intOr("x", 1));
    assertEquals(1, KeyValueFile.intOr(null, 1));
    assertEquals(9L, KeyValueFile.longOr("9", 2L));
    assertEquals(2L, KeyValueFile.longOr("?", 2L));
    assertEquals(2L, KeyValueFile.longOr(null, 2L));
  }
}
