package view;

import java.util.function.Consumer;

/** What the tabs bar asks its owner (the {@link NoteSession}) to do with the notes behind it. */
record TabRequests(Runnable newTab, Consumer<String> close, Consumer<String> closeOthers) {

  static final TabRequests NONE = new TabRequests(() -> {}, id -> {}, id -> {});
}
