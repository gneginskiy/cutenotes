package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class EdtReadTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  /** Mirrors the exit path: the EDT is stuck (in System.exit) while a hook asks for a read. */
  @Test
  void readFromAnotherThreadDoesNotHangWhenTheEdtIsBlocked() throws Exception {
    CountDownLatch release = new CountDownLatch(1);
    CountDownLatch blocked = new CountDownLatch(1);
    SwingUtilities.invokeLater(
        () -> {
          blocked.countDown();
          awaitQuietly(release);
        });
    blocked.await();
    try {
      String value =
          assertTimeoutPreemptively(
              Duration.ofSeconds(5), () -> EdtRead.onEdt(() -> "snapshot", 200));
      assertEquals("snapshot", value);
    } finally {
      release.countDown();
    }
  }

  @Test
  void readRunsOnTheEdtWhenItIsFree() {
    assertTrue(EdtRead.onEdt(SwingUtilities::isEventDispatchThread));
  }

  @Test
  void persistOnExitRunsOnlyOnceForWindowCloseAndShutdownHook() {
    AtomicInteger runs = new AtomicInteger();
    AppExit exit = new AppExit(() -> runs.incrementAndGet() > 0);

    exit.persistOnce();
    exit.persistOnce();

    assertEquals(1, runs.get());
  }

  @Test
  void failedOrCrashingFinalSaveIsReportedSoTheUserCanCancelQuitting() {
    assertFalse(new AppExit(() -> false).persistOnce());
    assertFalse(
        new AppExit(
                () -> {
                  throw new IllegalStateException("disk full");
                })
            .persistOnce());
  }

  private static void awaitQuietly(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
