package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import model.GroupData;
import model.TabMeta;
import util.NoteIndex;

class BrowserSearchTest {

  private final TabMeta pool = new TabMeta("a", "Backend", Instant.parse("2026-01-01T00:00:00Z"));
  private final TabMeta shop = new TabMeta("b", "Groceries", Instant.parse("2026-03-01T00:00:00Z"));
  private final TabMeta ideas = new TabMeta("c", "Ideas", Instant.parse("2026-02-01T00:00:00Z"));
  private final NoteIndex index =
      NoteIndex.of(
          Map.of(
              "a", "Increase the connection pool #work",
              "b", "milk, eggs #home",
              "c", "a startup about pools"));

  @Test
  void findsNotesByTextOnceTheIndexIsThereAndShowsWhere() {
    NoteFilter byName = NoteFilter.all(Set.of()).withQuery("pool");
    NoteFilter byText = byName.withIndex(index);

    assertFalse(byName.accepts(pool), "no index yet: names only");
    assertTrue(byText.accepts(pool));
    assertTrue(byText.accepts(ideas));
    assertFalse(byText.accepts(shop));
    assertTrue(byText.snippet(pool).contains("connection pool"));
    assertNull(byText.withQuery("backend").snippet(pool), "a name match needs no snippet");
  }

  @Test
  void hashQueriesFilterByTagOnly() {
    NoteFilter tag = NoteFilter.all(Set.of()).withIndex(index).withQuery("#work");

    assertTrue(tag.tagQuery());
    assertTrue(tag.accepts(pool));
    assertFalse(tag.accepts(shop));
    assertFalse(tag.nameMatches("work"), "group names never match a tag query");
    assertNull(tag.snippet(pool));
  }

  @Test
  void pinnedNotesComeFirstThenTheChosenOrder() {
    NoteMetas metas = NoteMetas.inMemory();
    metas.put(metas.get("c").withPinned(true));
    BrowserQuery query = new BrowserQuery(Set.of(), metas);
    GroupData data = GroupData.EMPTY.withOrder(List.of("b", "a"));

    assertEquals(List.of("c", "b", "a"), sorted(query, data));
    query.setSort(BrowserQuery.Sort.RECENT);
    assertEquals(List.of("c", "b", "a"), sorted(query, data));
    query.setSort(BrowserQuery.Sort.NAME);
    assertEquals(List.of("c", "a", "b"), sorted(query, data));
    assertFalse(query.reorderable(), "dragging only in manual order");
    query.setSort(BrowserQuery.Sort.MANUAL);
    assertTrue(query.reorderable());
    query.change(f -> f.withQuery("x"));
    assertFalse(query.reorderable(), "nor while searching");
  }

  private List<String> sorted(BrowserQuery query, GroupData data) {
    return List.of(pool, shop, ideas).stream().sorted(query.order(data)).map(TabMeta::id).toList();
  }
}
