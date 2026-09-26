package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import model.Tab;

class NoteIndexTest {

  private final NoteIndex index = NoteIndex.of(contents());

  @Test
  void findsTextCaseInsensitivelyIgnoringMarkdown() {
    assertTrue(index.contains("a", "CONNECTION pool"));
    assertTrue(index.contains("a", "important"));
    assertFalse(index.contains("a", "**important**"));
    assertFalse(index.contains("missing", "x"));
    assertFalse(index.contains("a", ""));
  }

  @Test
  void tagsAreSearchableAndCounted() {
    assertTrue(index.hasTag("a", "Work"));
    assertTrue(index.hasTag("b", "work"));
    assertFalse(index.hasTag("b", "home"));
    assertEquals(Map.of("home", 1, "work", 2), index.tagCounts());
  }

  @Test
  void lockedNotesAreNeverSearchedByText() {
    assertTrue(index.locked("c"));
    assertFalse(index.contains("c", "secret"));
    assertFalse(index.locked("a"));
    assertFalse(index.locked("missing"));
  }

  @Test
  void snippetShowsTheMatchInContext() {
    String snippet = index.snippet("a", "pool");

    assertTrue(snippet.contains("connection pool"));
    assertTrue(snippet.startsWith("…") || snippet.startsWith("Increase"));
    assertNull(index.snippet("a", "absent"));
    assertNull(index.snippet("missing", "x"));
  }

  @Test
  void buildsFromARepositorySkippingTheScratchArea() {
    RecordingRepo repo = new RecordingRepo();
    repo.save("n", "Note", "hello #tag");
    repo.save(Tab.DEFAULT_ID, "default", "scratch words");

    NoteIndex built = NoteIndex.build(repo);

    assertTrue(built.contains("n", "hello"));
    assertFalse(built.contains(Tab.DEFAULT_ID, "scratch"));
  }

  private static Map<String, String> contents() {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("a", "Increase the connection pool size, **important** #work #home");
    m.put("b", "groceries #WORK");
    m.put("c", NoteCrypto.encrypt("secret #hidden", NoteCrypto.newKey("pw".toCharArray())));
    return m;
  }
}
