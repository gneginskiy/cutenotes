package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable note-grouping state: a forest of {@link Group}s, a note-id to group-id assignment, and
 * an explicit display order of note ids. Notes absent from {@code assignments} are ungrouped; notes
 * absent from {@code order} fall back to their natural (date) order. Every mutator returns a copy.
 */
public record GroupData(List<Group> groups, Map<String, String> assignments, List<String> order) {

  public static final GroupData EMPTY = new GroupData(List.of(), Map.of(), List.of());

  public GroupData {
    groups = List.copyOf(groups);
    assignments = Map.copyOf(assignments);
    order = List.copyOf(order);
  }

  public GroupData withGroup(String id, String name, String parentId) {
    if (name == null || name.isBlank() || find(id) != null) {
      return this;
    }
    List<Group> next = new ArrayList<>(groups);
    next.add(new Group(id, name.trim(), parentId));
    return new GroupData(next, assignments, order);
  }

  public GroupData renamed(String id, String name) {
    if (name == null || name.isBlank() || find(id) == null) {
      return this;
    }
    return mapGroups(g -> g.id().equals(id) ? new Group(id, name.trim(), g.parentId()) : g);
  }

  public GroupData reparented(String id, String newParentId) {
    if (find(id) == null || (newParentId != null && find(newParentId) == null)) {
      return this;
    }
    if (id.equals(newParentId) || isUnder(newParentId, id)) {
      return this;
    }
    return mapGroups(g -> g.id().equals(id) ? new Group(id, g.name(), newParentId) : g);
  }

  public GroupData removed(String id) {
    Set<String> doomed = new HashSet<>();
    collectSubtree(id, doomed);
    List<Group> next = new ArrayList<>();
    for (Group g : groups) {
      if (!doomed.contains(g.id())) {
        next.add(g);
      }
    }
    Map<String, String> map = new LinkedHashMap<>(assignments);
    map.entrySet().removeIf(e -> doomed.contains(e.getValue()));
    return new GroupData(next, map, order);
  }

  public GroupData assign(String noteId, String groupId) {
    Map<String, String> map = new LinkedHashMap<>(assignments);
    if (groupId == null || find(groupId) == null) {
      map.remove(noteId);
    } else {
      map.put(noteId, groupId);
    }
    return new GroupData(groups, map, order);
  }

  public GroupData withOrder(List<String> order) {
    return new GroupData(groups, assignments, order);
  }

  public List<Group> childrenOf(String parentId) {
    List<Group> out = new ArrayList<>();
    for (Group g : groups) {
      if (Objects.equals(g.parentId(), parentId)) {
        out.add(g);
      }
    }
    return out;
  }

  public String groupOf(String noteId) {
    return assignments.get(noteId);
  }

  public int orderIndex(String noteId) {
    int idx = order.indexOf(noteId);
    return idx < 0 ? Integer.MAX_VALUE : idx;
  }

  public Group find(String id) {
    for (Group g : groups) {
      if (g.id().equals(id)) {
        return g;
      }
    }
    return null;
  }

  private GroupData mapGroups(java.util.function.UnaryOperator<Group> op) {
    List<Group> next = new ArrayList<>();
    for (Group g : groups) {
      next.add(op.apply(g));
    }
    return new GroupData(next, assignments, order);
  }

  private void collectSubtree(String id, Set<String> acc) {
    acc.add(id);
    for (Group g : groups) {
      if (id.equals(g.parentId())) {
        collectSubtree(g.id(), acc);
      }
    }
  }

  private boolean isUnder(String candidate, String ancestor) {
    String cursor = candidate;
    while (cursor != null) {
      if (cursor.equals(ancestor)) {
        return true;
      }
      Group g = find(cursor);
      cursor = g == null ? null : g.parentId();
    }
    return false;
  }
}
