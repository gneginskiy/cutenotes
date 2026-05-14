package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import dao.TabRepository;
import lombok.RequiredArgsConstructor;
import model.Tab;

@RequiredArgsConstructor
public class AutoSaver {

  private static final long INTERVAL_MS = 300;

  private final Supplier<List<Tab>> snapshotSupplier;
  private final TabRepository repo;
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          Thread.ofVirtual().name("auto-saver-", 0).factory());
  private final Map<String, Tab> lastSaved = new HashMap<>();

  public void start() {
    scheduler.scheduleWithFixedDelay(this::tick, INTERVAL_MS, INTERVAL_MS, TimeUnit.MILLISECONDS);
  }

  public void flush() {
    tick();
  }

  void tick() {
    for (Tab tab : snapshotSupplier.get()) {
      if (!tab.equals(lastSaved.get(tab.id()))) {
        repo.save(tab.id(), tab.name(), tab.content());
        lastSaved.put(tab.id(), tab);
      }
    }
  }
}
