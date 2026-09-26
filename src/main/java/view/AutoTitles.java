package view;

import static view.Messages.tr;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.Timer;

import markdown.TitleGuess;

/**
 * Names notes after their first line, like Apple Notes: a new "untitled" tab follows what is typed
 * until the user renames it. Renaming is debounced, so the note's file is not renamed per
 * keystroke.
 */
final class AutoTitles {

  static final int DELAY_MS = 600;

  private final NoteMetas metas;
  private final BiConsumer<String, String> rename;
  private final Map<String, Timer> timers = new HashMap<>();

  AutoTitles(NoteMetas metas, BiConsumer<String, String> rename) {
    this.metas = metas;
    this.rename = rename;
  }

  /** Starts following {@code area} when the note is new, untitled or was automatic before. */
  void attach(String id, String name, NoteEditor area) {
    if (isUntitled(name) && !metas.get(id).autoTitle()) {
      metas.put(metas.get(id).withAutoTitle(true));
    }
    Timer timer = new Timer(DELAY_MS, e -> retitle(id, area));
    timer.setRepeats(false);
    timers.put(id, timer);
    area.getDocument().addDocumentListener(DocumentChanges.on(timer::restart));
  }

  /** Applies the derived name now (tests, closing a tab). */
  void retitle(String id, NoteEditor area) {
    if (!metas.get(id).autoTitle()) {
      return;
    }
    String title = TitleGuess.from(area.getText());
    if (!title.isEmpty()) {
      rename.accept(id, title);
    }
  }

  /** The user named the note: stop following its first line. */
  void manual(String id) {
    metas.put(metas.get(id).withAutoTitle(false));
  }

  void detach(String id) {
    Timer timer = timers.remove(id);
    if (timer != null) {
      timer.stop();
    }
  }

  static boolean isUntitled(String name) {
    return name == null
        || name.isBlank()
        || "untitled".equalsIgnoreCase(name)
        || tr("tab.untitled").equalsIgnoreCase(name);
  }
}
