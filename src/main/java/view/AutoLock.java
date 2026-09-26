package view;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.time.Duration;
import java.time.Instant;
import javax.swing.Timer;

import util.Keyring;

/** Locks the unlocked protected notes after the idle time set in the options. */
final class AutoLock {

  private static final int CHECK_MS = 15_000;

  private AutoLock() {}

  static void install(Keyring keyring, Runnable lockAll) {
    Toolkit.getDefaultToolkit()
        .addAWTEventListener(
            e -> keyring.touch(Instant.now()), AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    Timer timer =
        new Timer(
            CHECK_MS,
            e -> {
              Duration idle = Duration.ofMinutes(SettingsHolder.current().autoLockMinutes());
              if (keyring.dueToLock(idle, Instant.now())) {
                lockAll.run();
              }
            });
    timer.start();
  }
}
