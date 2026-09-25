package util;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dao.TabRepository;
import model.Tab;
import model.TabMeta;

/** In-memory {@link TabRepository} that counts writes and can be told to fail for chosen ids. */
public final class RecordingRepo implements TabRepository {

  public final Map<String, Tab> byId = new HashMap<>();
  public final Map<String, Integer> writeCount = new HashMap<>();
  public Set<String> failing = Set.of();

  @Override
  public List<TabMeta> listMeta() {
    return byId.values().stream().map(t -> new TabMeta(t.id(), t.name(), Instant.EPOCH)).toList();
  }

  @Override
  public Tab load(String id) {
    return byId.getOrDefault(id, new Tab(id, "", ""));
  }

  @Override
  public void save(String id, String name, String content) {
    if (failing.contains(id)) {
      throw new IllegalStateException("File name too long: " + id);
    }
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
