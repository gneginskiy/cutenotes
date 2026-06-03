package view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SearchEngineTest {

  @Test
  void caseInsensitiveByDefault() {
    List<int[]> hits = SearchEngine.findAll("Note note NOTE", "note");

    assertEquals(3, hits.size());
    assertArrayEquals(new int[] {0, 4}, hits.get(0));
    assertArrayEquals(new int[] {5, 9}, hits.get(1));
    assertArrayEquals(new int[] {10, 14}, hits.get(2));
  }

  @Test
  void caseSensitiveMatchesExactCaseOnly() {
    List<int[]> hits = SearchEngine.findAll("Note note NOTE", "note", true);

    assertEquals(1, hits.size());
    assertArrayEquals(new int[] {5, 9}, hits.get(0));
  }

  @Test
  void emptyQueryYieldsNoMatches() {
    assertTrue(SearchEngine.findAll("anything", "").isEmpty());
  }

  @Test
  void stepWrapsAround() {
    assertEquals(0, SearchEngine.step(2, 3, 1));
    assertEquals(2, SearchEngine.step(0, 3, -1));
    assertEquals(-1, SearchEngine.step(0, 0, 1));
  }
}
