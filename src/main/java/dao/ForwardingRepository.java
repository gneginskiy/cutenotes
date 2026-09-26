package dao;

import java.util.List;

import model.Tab;
import model.TabMeta;

/** A repository that hands everything to another one; decorators override what they change. */
public abstract class ForwardingRepository implements TabRepository {

  private final TabRepository inner;

  protected ForwardingRepository(TabRepository inner) {
    this.inner = inner;
  }

  protected TabRepository inner() {
    return inner;
  }

  @Override
  public List<TabMeta> listMeta() {
    return inner.listMeta();
  }

  @Override
  public Tab load(String id) {
    return inner.load(id);
  }

  @Override
  public void save(String id, String name, String content) {
    inner.save(id, name, content);
  }

  @Override
  public void delete(String id) {
    inner.delete(id);
  }

  @Override
  public String newId() {
    return inner.newId();
  }

  @Override
  public void trash(String id) {
    inner.trash(id);
  }

  @Override
  public TrashBin trashBin() {
    return inner.trashBin();
  }

  @Override
  public NoteHistory history() {
    return inner.history();
  }
}
