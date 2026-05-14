package view;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

class TabHistory {

  private final Deque<String> closed = new ArrayDeque<>();

  void record(String name) {
    closed.push(name);
  }

  String popReopenable(Collection<String> alreadyOpen) {
    while (!closed.isEmpty()) {
      String name = closed.pop();
      if (!alreadyOpen.contains(name)) {
        return name;
      }
    }
    return null;
  }
}
