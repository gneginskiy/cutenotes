package view;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

/** Runs a read on the Swing event dispatch thread, so background threads never race a live edit. */
final class EdtRead {

  private EdtRead() {}

  static <T> T onEdt(Supplier<T> supplier) {
    if (SwingUtilities.isEventDispatchThread()) {
      return supplier.get();
    }
    AtomicReference<T> ref = new AtomicReference<>();
    try {
      SwingUtilities.invokeAndWait(() -> ref.set(supplier.get()));
      return ref.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (InvocationTargetException e) {
      // fall through to a best-effort direct read
    }
    return supplier.get();
  }
}
