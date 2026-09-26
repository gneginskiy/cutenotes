package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import dao.ForwardingRepository;
import dao.TabRepository;
import model.Tab;

/**
 * Encrypts the notes whose key is in the {@link Keyring} on save and decrypts them on load. A
 * protected note whose key is not there loads as its encrypted text and is never overwritten by
 * plain text: only {@link #unprotect} removes the protection.
 */
public final class EncryptingRepository extends ForwardingRepository {

  private static final Logger LOG = Logger.getLogger(EncryptingRepository.class.getName());

  private final Keyring keyring;

  /** Whether each note seen so far is protected on disk, so saves need not read the file. */
  private final Map<String, Boolean> protectedOnDisk = new ConcurrentHashMap<>();

  public EncryptingRepository(TabRepository inner, Keyring keyring) {
    super(inner);
    this.keyring = keyring;
  }

  @Override
  public Tab load(String id) {
    Tab stored = super.load(id);
    protectedOnDisk.put(id, NoteCrypto.isEncrypted(stored.content()));
    NoteCrypto.Key key = keyring.key(id);
    if (key == null || !NoteCrypto.isEncrypted(stored.content())) {
      return stored;
    }
    return new Tab(id, stored.name(), NoteCrypto.decrypt(stored.content(), key));
  }

  @Override
  public void save(String id, String name, String content) {
    NoteCrypto.Key key = keyring.key(id);
    if (key != null && !NoteCrypto.isEncrypted(content)) {
      super.save(id, name, NoteCrypto.encrypt(content, key));
    } else if (key != null || !isProtectedOnDisk(id)) {
      super.save(id, name, content);
    } else {
      LOG.warning("kept a locked note from being overwritten with plain text: " + id);
    }
  }

  /** Protects a note: from now on it is stored encrypted with {@code key}. */
  public void protect(String id, String name, String content, NoteCrypto.Key key) {
    keyring.put(id, key);
    save(id, name, content);
    protectedOnDisk.put(id, true);
  }

  /** Removes a note's password: it is stored as plain text again. */
  public void unprotect(String id, String name, String content) {
    keyring.remove(id);
    super.save(id, name, content);
    protectedOnDisk.put(id, false);
  }

  /** Whether the note on disk is password protected. */
  public boolean isProtectedOnDisk(String id) {
    return protectedOnDisk.computeIfAbsent(id, i -> NoteCrypto.isEncrypted(storedText(i)));
  }

  /** The note's text as stored: encrypted if it is protected. */
  public String storedText(String id) {
    return super.load(id).content();
  }

  public Keyring keyring() {
    return keyring;
  }
}
