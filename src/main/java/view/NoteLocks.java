package view;

import static view.Messages.tr;

import java.awt.Component;
import java.util.Arrays;

import model.Tab;
import util.EncryptingRepository;
import util.NoteCrypto;

/**
 * Password protection in the window: unlocking a protected note when it opens, protecting and
 * unprotecting the current note, and locking every unlocked note (closing its tab).
 */
final class NoteLocks {

  private final Component parent;
  private final EncryptingRepository repo;
  private final NoteSession session;
  private final TabsPane tabs;
  private final Toast toast;

  NoteLocks(
      Component parent,
      EncryptingRepository repo,
      NoteSession session,
      TabsPane tabs,
      Toast toast) {
    this.parent = parent;
    this.repo = repo;
    this.session = session;
    this.tabs = tabs;
    this.toast = toast;
    session.setUnlocker(this::unlock);
  }

  /** The readable note, asking for its password if it is protected; {@code null} if cancelled. */
  Tab unlock(Tab stored) {
    return NoteCrypto.isEncrypted(stored.content())
        ? unlockWith(stored, tr("lock.unlock.title", stored.name()))
        : stored;
  }

  private Tab unlockWith(Tab stored, String title) {
    String error = null;
    while (true) {
      char[] password = PasswordPrompt.ask(parent, title, error);
      if (password == null) {
        return null;
      }
      try {
        NoteCrypto.Key key = NoteCrypto.keyFor(password, stored.content());
        String plain = NoteCrypto.decrypt(stored.content(), key);
        repo.keyring().put(stored.id(), key);
        return new Tab(stored.id(), stored.name(), plain);
      } catch (IllegalArgumentException wrong) {
        error = tr("lock.wrong");
      } finally {
        Arrays.fill(password, '\0');
      }
    }
  }

  boolean activeProtected() {
    return repo.keyring().unlocked(tabs.activeId());
  }

  /** Protects the current note with a new password. */
  void protect() {
    Tab tab = tabs.snapshotOf(tabs.activeId());
    if (Tab.DEFAULT_ID.equals(tab.id())) {
      toast.flash(tr("lock.default"));
      return;
    }
    char[] password = PasswordPrompt.askNew(parent, tr("lock.set.title", tab.name()));
    if (password == null) {
      return;
    }
    NoteCrypto.Key key = NoteCrypto.newKey(password);
    Arrays.fill(password, '\0');
    repo.protect(tab.id(), tab.name(), tab.content(), key);
    repo.history().deleteAll(tab.id());
    toast.flash(tr("lock.protected", tab.name()));
  }

  /** Removes the current note's password after asking for it once more. */
  void removePassword() {
    Tab tab = tabs.snapshotOf(tabs.activeId());
    Tab stored = new Tab(tab.id(), tab.name(), repo.storedText(tab.id()));
    if (unlockWith(stored, tr("lock.remove.title", tab.name())) != null) {
      repo.unprotect(tab.id(), tab.name(), tab.content());
      toast.flash(tr("lock.removed"));
    }
  }

  /** Closes every unlocked note (saving it encrypted) and forgets the keys. */
  void lockAll() {
    if (!anyUnlocked()) {
      return;
    }
    for (String id : repo.keyring().ids()) {
      session.close(id);
    }
    repo.keyring().clear();
    toast.flash(tr("lock.locked"));
  }

  boolean anyUnlocked() {
    return !repo.keyring().ids().isEmpty();
  }
}
