package dao;

import java.time.Clock;

import model.Tab;
import util.NoteCrypto;

/**
 * A repository that also records every save in the {@link HistoryStore}. Password-protected text is
 * never recorded: the history would outlive a later change of the password.
 */
public final class HistoryRepository extends ForwardingRepository {

  private final HistoryStore history;
  private final Clock clock;

  public HistoryRepository(TabRepository inner, HistoryStore history) {
    this(inner, history, Clock.systemUTC());
  }

  HistoryRepository(TabRepository inner, HistoryStore history, Clock clock) {
    super(inner);
    this.history = history;
    this.clock = clock;
  }

  @Override
  public void save(String id, String name, String content) {
    super.save(id, name, content);
    boolean recordable = !content.isBlank() && !NoteCrypto.isEncrypted(content);
    if (!Tab.DEFAULT_ID.equals(id) && recordable) {
      history.snapshot(id, content, clock.instant(), false);
    }
  }

  @Override
  public void delete(String id) {
    super.delete(id);
    history.deleteAll(id);
  }

  @Override
  public HistoryStore history() {
    return history;
  }
}
