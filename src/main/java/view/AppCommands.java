package view;

import static view.Messages.tr;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;

import util.DroppedFile;
import util.TextStats;

/** What the menus, shortcuts and tray do in the main window. */
final class AppCommands {

  private final AppContext c;
  private final NoteLocks locks;

  AppCommands(AppContext context) {
    this.c = context;
    this.locks = new NoteLocks(c.frame(), c.repo(), c.session(), c.tabs(), c.toast());
    AutoLock.install(c.repo().keyring(), this::lockAll);
    SettingsHooks.install(c, new AppTray(c.frame(), this::newTab));
    WindowReopen.install(c.frame());
    FileDrop.use(this::openFile, c.toast()::flash);
  }

  /** A text file dropped on the window becomes a new note named after it. */
  void openFile(File file) {
    try {
      String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
      c.session().newTab(DroppedFile.title(file.getName()), text);
    } catch (IOException | RuntimeException e) {
      c.toast().flash(tr("drop.failed", file.getName()));
    }
  }

  NoteLocks locks() {
    return locks;
  }

  /** Protects the current note with a password, or removes its password. */
  void togglePassword() {
    if (locks.activeProtected()) {
      locks.removePassword();
    } else {
      locks.protect();
    }
  }

  void lockAll() {
    locks.lockAll();
  }

  AppContext context() {
    return c;
  }

  void newTab() {
    c.session().newTab();
  }

  void browse() {
    NotesBrowserDialog.open(
        c.frame(),
        new NotesBrowserDialog.Source(
            c.repo(), c.groups(), c.tabs().identity().metas(), new HashSet<>(c.tabs().openIds())),
        new NotesBrowserDialog.Callbacks(
            c.session()::openExisting,
            this::openFound,
            c.session()::delete,
            id -> c.tabs().identity().refreshColor(id),
            this::showTrash));
  }

  void showTrash() {
    TrashDialog.open(c.frame(), c.repo().trashBin(), c.session()::openExisting);
  }

  void export() {
    String id = c.tabs().activeId();
    NoteExport.export(
        c.frame(), c.tabs().snapshotOf(id).name(), c.tabs().activeArea().markdown(), c.toast());
  }

  void print() {
    NoteExport.print(c.frame(), c.tabs().activeArea().markdown(), ThemeHolder.current());
  }

  /** Earlier versions of the current note. */
  void showHistory() {
    TabsPane tabs = c.tabs();
    String id = tabs.activeId();
    HistoryDialog.open(
        c.frame(), c.repo().history(), id, tabs.snapshotOf(id).name(), tabs.activeArea());
  }

  void reopenLast() {
    if (!c.session().reopenLastClosed()) {
      c.toast().flash(tr("toast.noClosedTabs"));
    }
  }

  void closeTab() {
    c.session().closeCurrent();
  }

  /** Opens a note found by its text, with the search term highlighted. */
  void openFound(String id, String query) {
    c.session().openExisting(id);
    c.search().openWith(query);
  }

  void selectNext() {
    c.tabs().selectRelative(1);
  }

  void selectPrevious() {
    c.tabs().selectRelative(-1);
  }

  /** Tab 1–8 by position; 9 is always the last tab, as in browsers. */
  void selectTab(int number) {
    c.tabs().selectPosition(number >= 9 ? -1 : number - 1);
  }

  void zoom(int delta) {
    c.toast().flash(Zoom.step(c.options(), delta));
  }

  void wordCount() {
    NoteEditor editor = c.tabs().activeArea();
    String selected = editor.getSelectedText();
    TextStats stats = TextStats.of(selected != null ? selected : editor.getText());
    String key = selected != null ? "toast.selectionWordCount" : "toast.wordCount";
    c.toast().flash(tr(key, stats.words(), stats.characters()));
  }
}
