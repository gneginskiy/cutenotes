package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import dao.TabRepository;
import model.Tab;
import model.TabMeta;

class AutoSaverTest {

  @Test
  void persistsEachTabOnFirstTick() {
    RecordingRepo repo = new RecordingRepo();
    AutoSaver saver =
        new AutoSaver(
            () -> List.of(new Tab("a", "Alpha", "AA"), new Tab("b", "Bravo", "BB")), repo);

    saver.tick();

    assertEquals("AA", repo.byId.get("a").content());
    assertEquals("BB", repo.byId.get("b").content());
  }

  @Test
  void skipsUnchangedTabs() {
    RecordingRepo repo = new RecordingRepo();
    AutoSaver saver = new AutoSaver(() -> List.of(new Tab("a", "n", "x")), repo);

    saver.tick();
    saver.tick();
    saver.tick();

    assertEquals(1, repo.writeCount.get("a"));
  }

  @Test
  void persistsAgainOnContentChange() {
    RecordingRepo repo = new RecordingRepo();
    AtomicReference<List<Tab>> source = new AtomicReference<>(List.of(new Tab("a", "n", "1")));
    AutoSaver saver = new AutoSaver(source::get, repo);

    saver.tick();
    source.set(List.of(new Tab("a", "n", "2")));
    saver.tick();

    assertEquals(2, repo.writeCount.get("a"));
    assertEquals("2", repo.byId.get("a").content());
  }

  @Test
  void persistsAgainOnNameChange() {
    RecordingRepo repo = new RecordingRepo();
    AtomicReference<List<Tab>> source = new AtomicReference<>(List.of(new Tab("a", "old", "x")));
    AutoSaver saver = new AutoSaver(source::get, repo);

    saver.tick();
    source.set(List.of(new Tab("a", "new", "x")));
    saver.tick();

    assertEquals("new", repo.byId.get("a").name());
  }

  @Test
  void flushDelegatesToTick() {
    RecordingRepo repo = new RecordingRepo();
    AutoSaver saver = new AutoSaver(() -> List.of(new Tab("a", "n", "z")), repo);

    saver.flush();

    assertEquals("z", repo.byId.get("a").content());
  }

  private static final class RecordingRepo implements TabRepository {
    final Map<String, Tab> byId = new HashMap<>();
    final Map<String, Integer> writeCount = new HashMap<>();

    @Override
    public List<TabMeta> listMeta() {
      return List.of();
    }

    @Override
    public Tab load(String id) {
      return byId.getOrDefault(id, new Tab(id, "", ""));
    }

    @Override
    public void save(String id, String name, String content) {
      byId.put(id, new Tab(id, name, content));
      writeCount.merge(id, 1, Integer::sum);
    }

    @Override
    public void delete(String id) {
      byId.remove(id);
    }

    @Override
    public String newId() {
      return UUID.randomUUID().toString();
    }
  }
}
