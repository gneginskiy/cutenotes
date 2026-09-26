package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import model.TabMeta;

class DiskChangeTest {

  @Test
  void reloadsWithoutUnsavedEditsAndKeepsBothOtherwise() {
    assertEquals(DiskChange.NONE, DiskChange.decide("a", "a", "a edited"));
    assertEquals(DiskChange.NONE, DiskChange.decide("a", "b", "b"));
    assertEquals(DiskChange.RELOAD, DiskChange.decide("a", "b", "a"));
    assertEquals(DiskChange.CONFLICT, DiskChange.decide("a", "b", "c"));
  }

  @Test
  void reportsOpenNotesWhoseFileTimeChanged() {
    Instant t0 = Instant.parse("2026-09-26T10:00:00Z");
    Instant t1 = t0.plusSeconds(5);
    ModTimes times = new ModTimes();
    Set<String> open = Set.of("a", "b");

    assertEquals(List.of(), times.changed(metas(t0, t0, t0), open), "first look only watches");
    assertEquals(List.of(), times.changed(metas(t0, t0, t1), open), "c is not open");
    assertEquals(List.of("a"), times.changed(metas(t1, t0, t1), open));
    assertEquals(List.of(), times.changed(metas(t1, t0, t1), open));
  }

  private static List<TabMeta> metas(Instant a, Instant b, Instant c) {
    return List.of(new TabMeta("a", "A", a), new TabMeta("b", "B", b), new TabMeta("c", "C", c));
  }
}
