package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GroupDataTest {

  @Test
  void withGroupAddsTopLevelAndIgnoresBlankOrDuplicateId() {
    GroupData d =
        GroupData.EMPTY
            .withGroup("g1", "Work", null)
            .withGroup("g1", "Dup", null)
            .withGroup("g2", "  ", null);

    assertEquals(1, d.childrenOf(null).size());
    assertEquals("Work", d.find("g1").name());
  }

  @Test
  void nestedGroupsResolveViaChildrenOf() {
    GroupData d = GroupData.EMPTY.withGroup("g1", "Work", null).withGroup("g2", "Projects", "g1");

    assertEquals(List.of("g1"), d.childrenOf(null).stream().map(Group::id).toList());
    assertEquals(List.of("g2"), d.childrenOf("g1").stream().map(Group::id).toList());
  }

  @Test
  void assignStoresAndUngroupsMembership() {
    GroupData d = GroupData.EMPTY.withGroup("g1", "Work", null).assign("n1", "g1");
    assertEquals("g1", d.groupOf("n1"));

    assertNull(d.assign("n1", null).groupOf("n1"));
  }

  @Test
  void assignToUnknownGroupUngroups() {
    GroupData d = GroupData.EMPTY.assign("n1", "ghost");

    assertNull(d.groupOf("n1"));
  }

  @Test
  void renameKeepsIdAndParent() {
    GroupData d = GroupData.EMPTY.withGroup("g1", "Work", null).renamed("g1", "Job");

    assertEquals("Job", d.find("g1").name());
  }

  @Test
  void reparentMovesUnderNewParent() {
    GroupData d =
        GroupData.EMPTY
            .withGroup("g1", "Work", null)
            .withGroup("g2", "Projects", null)
            .reparented("g2", "g1");

    assertEquals("g1", d.find("g2").parentId());
  }

  @Test
  void reparentRejectsCycles() {
    GroupData d =
        GroupData.EMPTY
            .withGroup("g1", "Work", null)
            .withGroup("g2", "Sub", "g1")
            .reparented("g1", "g2");

    assertNull(d.find("g1").parentId());
  }

  @Test
  void removeDropsSubtreeAndUngroupsMembers() {
    GroupData d =
        GroupData.EMPTY
            .withGroup("g1", "Work", null)
            .withGroup("g2", "Sub", "g1")
            .assign("n1", "g2")
            .removed("g1");

    assertNull(d.find("g1"));
    assertNull(d.find("g2"));
    assertNull(d.groupOf("n1"));
  }

  @Test
  void withOrderDrivesOrderIndex() {
    GroupData d = GroupData.EMPTY.withOrder(List.of("c", "a", "b"));

    assertEquals(List.of("c", "a", "b"), d.order());
    assertEquals(0, d.orderIndex("c"));
    assertEquals(2, d.orderIndex("b"));
  }

  @Test
  void unorderedNoteReportsMaxIndex() {
    assertEquals(Integer.MAX_VALUE, GroupData.EMPTY.orderIndex("ghost"));
  }

  @Test
  void immutableCopiesAreReturned() {
    GroupData base = GroupData.EMPTY.withGroup("g1", "Work", null);
    GroupData next = base.withGroup("g2", "Personal", null);

    assertEquals(1, base.childrenOf(null).size());
    assertNotNull(next.find("g2"));
    assertTrue(next.childrenOf(null).size() == 2);
  }
}
