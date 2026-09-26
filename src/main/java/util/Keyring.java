package util;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The keys of the password-protected notes unlocked in this session, held in memory only, and when
 * the user last did something (for locking them again after a while).
 */
public final class Keyring {

  private final Map<String, NoteCrypto.Key> keys = new HashMap<>();
  private Instant lastActivity = Instant.now();

  public synchronized NoteCrypto.Key key(String id) {
    return keys.get(id);
  }

  public synchronized void put(String id, NoteCrypto.Key key) {
    keys.put(id, key);
  }

  public synchronized void remove(String id) {
    keys.remove(id);
  }

  public synchronized boolean unlocked(String id) {
    return keys.containsKey(id);
  }

  /** The unlocked notes. */
  public synchronized List<String> ids() {
    return List.copyOf(keys.keySet());
  }

  /** Forgets every key. */
  public synchronized void clear() {
    keys.clear();
  }

  public synchronized void touch(Instant now) {
    lastActivity = now;
  }

  /** Whether something is unlocked and nothing happened for {@code idle}; zero never locks. */
  public synchronized boolean dueToLock(Duration idle, Instant now) {
    return !keys.isEmpty() && !idle.isZero() && !lastActivity.plus(idle).isAfter(now);
  }
}
