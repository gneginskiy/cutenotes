package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import dao.SessionStore;
import dao.TabRepository;
import lombok.RequiredArgsConstructor;
import model.Tab;

/**
 * Persists changed tabs (and the list of open tabs) every {@value #INTERVAL_MS} ms. The snapshot is
 * taken outside the lock (it may block on the EDT); writes are serialised so a scheduled tick, an
 * exit flush and a deletion never interleave on the same file.
 */
@RequiredArgsConstructor
public class AutoSaver {

  private static final long INTERVAL_MS = 300;

  private final Supplier<List<Tab>> snapshotSupplier;
  private final TabRepository repo;
  private final SessionStore sessions;
  private final SaveListener listener;
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          Thread.ofVirtual().name("auto-saver-", 0).factory());
  private final Map<String, Tab> lastSaved = new HashMap<>();
  private final Set<String> discarded = new HashSet<>();
  private List<String> lastSession;
  private boolean failing;

  public AutoSaver(Supplier<List<Tab>> snapshotSupplier, TabRepository repo) {
    this(snapshotSupplier, repo, null, SaveListener.NONE);
  }

  public void start() {
    scheduler.scheduleWithFixedDelay(this::tick, INTERVAL_MS, INTERVAL_MS, TimeUnit.MILLISECONDS);
  }

  public void flush() {
    tick();
  }

  /** Records a tab just loaded from disk as persisted, so merely opening it never rewrites it. */
  public synchronized void markSaved(Tab tab) {
    lastSaved.put(tab.id(), tab);
    discarded.remove(tab.id());
  }

  /**
   * Runs {@code read} while no save can happen, so what it reads from disk and {@link #lastSaved}
   * describe the same moment.
   */
  public synchronized <T> T withoutSaving(Supplier<T> read) {
    return read.get();
  }

  /** The note as the app last wrote or read it; {@code null} if unknown. */
  public synchronized Tab lastSaved(String id) {
    return lastSaved.get(id);
  }

  /** Forgets a deleted note, so a snapshot taken just before the deletion cannot resurrect it. */
  public synchronized void discard(String id) {
    discarded.add(id);
    lastSaved.remove(id);
  }

  /** Saves a tab leaving the snapshot (e.g. being closed), serialised with the periodic ticks. */
  public synchronized void save(Tab tab) {
    try {
      saveIfChanged(tab);
    } catch (RuntimeException e) {
      report(e);
      throw e;
    }
  }

  /** Whether the latest save attempt failed for at least one note or the session. */
  public synchronized boolean isFailing() {
    return failing;
  }

  void tick() {
    List<Tab> snapshot;
    try {
      snapshot = snapshotSupplier.get();
    } catch (RuntimeException e) {
      return; // a transient read race must never kill the recurring task
    }
    persist(snapshot);
  }

  private synchronized void persist(List<Tab> snapshot) {
    Exception failure = null;
    for (Tab tab : snapshot) {
      try {
        saveIfChanged(tab);
      } catch (Exception e) {
        failure = e; // keep going: one unsavable tab must not block the others
      }
    }
    try {
      saveSession(snapshot);
    } catch (Exception e) {
      failure = e;
    }
    report(failure);
  }

  private void saveIfChanged(Tab tab) {
    Tab previous = lastSaved.get(tab.id());
    boolean neverSavedAndEmpty = previous == null && tab.content().isBlank();
    if (discarded.contains(tab.id()) || tab.equals(previous) || neverSavedAndEmpty) {
      return;
    }
    repo.save(tab.id(), tab.name(), tab.content());
    lastSaved.put(tab.id(), tab);
  }

  private void saveSession(List<Tab> snapshot) {
    if (sessions == null) {
      return;
    }
    List<String> ids =
        snapshot.stream()
            .filter(t -> t.inSession() && !discarded.contains(t.id()))
            .map(Tab::id)
            .toList();
    if (!ids.equals(lastSession)) {
      sessions.write(ids);
      lastSession = ids;
    }
  }

  private void report(Exception failure) {
    if ((failure != null) != failing) {
      failing = failure != null;
      listener.onSaveStatus(failure);
    }
  }
}
