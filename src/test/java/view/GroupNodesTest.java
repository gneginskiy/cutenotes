package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.jupiter.api.Test;

import model.Group;
import model.GroupData;
import model.TabMeta;

class GroupNodesTest {

  private final TabMeta groceries = note("g", "Groceries");
  private final TabMeta recipes = note("r", "Pasta recipes");
  private final TabMeta ideas = note("i", "Startup ideas");
  private final List<TabMeta> notes = List.of(groceries, recipes, ideas);
  private final GroupData data =
      GroupData.EMPTY
          .withGroup("home", "Home", null)
          .withGroup("food", "Kitchen", "home")
          .assign("g", "home")
          .assign("r", "food");

  @Test
  void withoutAQueryEveryGroupAndNoteIsListed() {
    DefaultMutableTreeNode root = GroupNodes.root(data, notes, NoteFilter.all(Set.of()), "Other");

    assertEquals(List.of("r", "g", "i"), GroupNodes.noteOrder(root));
    assertEquals(List.of("Home", "Other"), topLevelNames(root));
  }

  @Test
  void searchKeepsOnlyMatchesAndTheGroupsLeadingToThem() {
    NoteFilter filter = NoteFilter.all(Set.of()).withQuery("  PASTA ");

    DefaultMutableTreeNode root = GroupNodes.root(data, notes, filter, "Other");

    assertEquals(List.of("r"), GroupNodes.noteOrder(root));
    assertEquals(List.of("Home"), topLevelNames(root), "empty buckets and groups are hidden");
  }

  @Test
  void aGroupWhoseNameMatchesShowsAllItsNotes() {
    NoteFilter filter = NoteFilter.all(Set.of()).withQuery("kitch");

    DefaultMutableTreeNode root = GroupNodes.root(data, notes, filter, "Other");

    assertEquals(List.of("r"), GroupNodes.noteOrder(root));
  }

  @Test
  void hidingOpenNotesStillAppliesWhileSearching() {
    NoteFilter filter = new NoteFilter(false, Set.of("i"), "i");

    assertFalse(filter.accepts(ideas));
    assertTrue(filter.accepts(recipes));
    assertTrue(filter.searching());
    assertFalse(filter.withQuery(" ").searching());
  }

  @Test
  void noMatchesLeavesAnEmptyTree() {
    NoteFilter filter = NoteFilter.all(Set.of()).withQuery("zzz");

    assertEquals(0, GroupNodes.root(data, notes, filter, "Other").getChildCount());
  }

  private static List<String> topLevelNames(DefaultMutableTreeNode root) {
    List<String> names = new ArrayList<>();
    for (int i = 0; i < root.getChildCount(); i++) {
      Object o = ((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject();
      names.add(o instanceof Group g ? g.name() : String.valueOf(o));
    }
    return names;
  }

  private static TabMeta note(String id, String name) {
    return new TabMeta(id, name, Instant.EPOCH);
  }
}
