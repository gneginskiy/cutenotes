package view;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

/**
 * Runs a read on the Swing event dispatch thread, so background threads never race a live edit. The
 * wait is bounded: when the EDT is blocked (e.g. it is inside {@code System.exit} waiting for the
 * very shutdown hook that asks for this read) an unbounded {@code invokeAndWait} deadlocks the
 * process. After the timeout the read is done directly — the EDT is not mutating anything then.
 */
final class EdtRead {

  static final long TIMEOUT_MS = 2000;

  private EdtRead() {}

  static <T> T onEdt(Supplier<T> supplier) {
    return onEdt(supplier, TIMEOUT_MS);
  }

  static <T> T onEdt(Supplier<T> supplier, long timeoutMs) {
    if (SwingUtilities.isEventDispatchThread()) {
      return supplier.get();
    }
    FutureTask<T> task = new FutureTask<>(supplier::get);
    SwingUtilities.invokeLater(task);
    try {
      return task.get(timeoutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException | TimeoutException e) {
      task.cancel(false);
    }
    return supplier.get();
  }
}
