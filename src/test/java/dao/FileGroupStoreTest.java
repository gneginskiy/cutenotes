package dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.Group;
import model.GroupData;

class FileGroupStoreTest {

  @Test
  void readsEmptyWhenMissing(@TempDir Path tmp) {
    assertSame(GroupData.EMPTY, new FileGroupStore(tmp.resolve("g.txt")).read());
  }

  @Test
  void roundTripKeepsNestedGroupsAndAssignments(@TempDir Path tmp) {
    FileGroupStore store = new FileGroupStore(tmp.resolve("g.txt"));
    GroupData data =
        new GroupData(
            List.of(new Group("g1", "Work", null), new Group("g2", "Projects", "g1")),
            Map.of("note-1", "g2"));

    store.write(data);
    GroupData loaded = store.read();

    assertEquals("Work", loaded.find("g1").name());
    assertNull(loaded.find("g1").parentId());
    assertEquals("g1", loaded.find("g2").parentId());
    assertEquals("g2", loaded.groupOf("note-1"));
  }

  @Test
  void keepsEmptyGroupWithNoMembers(@TempDir Path tmp) {
    FileGroupStore store = new FileGroupStore(tmp.resolve("g.txt"));

    store.write(new GroupData(List.of(new Group("g1", "Empty", null)), Map.of()));

    GroupData loaded = store.read();
    assertEquals(1, loaded.groups().size());
    assertEquals("Empty", loaded.find("g1").name());
  }

  @Test
  void defaultConstructorReadsWithoutError() {
    assertDoesNotThrow(() -> new FileGroupStore().read());
  }
}
