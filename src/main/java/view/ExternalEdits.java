package view;

import static view.Messages.tr;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import dao.TabRepository;
import model.Tab;
import util.AutoSaver;
import util.DiskChange;
import util.ModTimes;
import util.NoteCrypto;

/**
 * Notices open notes whose files changed outside the app (another editor, a sync service). Without
 * unsaved edits the tab shows the new text (undoable); with them, the tab keeps its text and the
 * disk version is saved as a separate note, so neither is lost.
 */
final class ExternalEdits {

  static final Duration EVERY = Duration.ofSeconds(2);
  private static final Logger LOG = Logger.getLogger(ExternalEdits.class.getName());

  private final TabRepository repo;
  private final TabsPane tabs;
  private final AutoSaver saver;
  private final Consumer<String> notifier;
  private final ModTimes times = new ModTimes();

  ExternalEdits(TabRepository repo, TabsPane tabs, AutoSaver saver, Consumer<String> notifier) {
    this.repo = repo;
    this.tabs = tabs;
    this.saver = saver;
    this.notifier = notifier;
  }

  void start() {
    Thread.ofVirtual().name("external-edits").start(this::watch);
  }

  private void watch() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(EVERY);
        check();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (RuntimeException e) {
        LOG.log(Level.FINE, "checking for outside changes failed", e);
      }
    }
  }

  /** Looks once for changed files (off the EDT) and applies what it finds on the EDT. */
  void check() {
    List<String> open = EdtRead.onEdt(tabs::openIds);
    for (String id : times.changed(repo.listMeta(), open)) {
      Tab disk = repo.load(id);
      Tab known = saver.lastSaved(id);
      if (known != null && !NoteCrypto.isEncrypted(disk.content())) {
        SwingUtilities.invokeLater(() -> apply(disk, known));
      }
    }
  }

  void apply(Tab disk, Tab known) {
    Tab current = tabs.snapshotOf(disk.id());
    if (current == null) {
      return;
    }
    switch (DiskChange.decide(known.content(), disk.content(), current.content())) {
      case RELOAD -> {
        tabs.areaOf(disk.id()).replaceText(disk.content());
        saver.markSaved(tabs.snapshotOf(disk.id()));
        notifier.accept(tr("toast.reloaded", current.name()));
      }
      case CONFLICT -> {
        String copyName = tr("conflict.name", current.name());
        repo.save(repo.newId(), copyName, disk.content());
        notifier.accept(tr("toast.conflict", copyName));
      }
      case NONE -> {
        // nothing new
      }
    }
  }
}
