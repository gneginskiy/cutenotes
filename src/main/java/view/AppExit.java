package view;

import java.awt.Desktop;
import java.awt.desktop.QuitStrategy;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Saves everything exactly once on the way out. Closing the window persists on the EDT and then
 * exits; the shutdown hook only covers external termination (logout, SIGTERM). Previously the
 * window used {@code EXIT_ON_CLOSE}: {@code System.exit} ran on the EDT while the hook waited for
 * the EDT — the process hung and the last edits plus the open-tabs list were never written.
 */
final class AppExit {

  private final AtomicBoolean persisted = new AtomicBoolean();
  private final BooleanSupplier persist;

  /** {@code persist} returns whether everything reached the disk. */
  AppExit(BooleanSupplier persist) {
    this.persist = persist;
  }

  /** {@code quitUnsaved} asks the user whether to quit although the final save failed. */
  static void install(JFrame frame, BooleanSupplier persist, BooleanSupplier quitUnsaved) {
    AppExit exit = new AppExit(persist);
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            if (exit.persistOnce() || quitUnsaved.getAsBoolean()) {
              frame.dispose();
              System.exit(0);
            }
            exit.persisted.set(false);
          }
        });
    Runtime.getRuntime().addShutdownHook(new Thread(exit::persistOnce, "persist-on-exit"));
    routeMacQuitThroughWindowClosing();
  }

  /** Persists unless already done; returns {@code false} only when this attempt failed. */
  boolean persistOnce() {
    if (persisted.getAndSet(true)) {
      return true;
    }
    try {
      return persist.getAsBoolean();
    } catch (RuntimeException e) {
      return false;
    }
  }

  /** Cmd+Q would call {@code System.exit} directly; closing the windows goes through our save. */
  private static void routeMacQuitThroughWindowClosing() {
    if (Desktop.isDesktopSupported()
        && Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_STRATEGY)) {
      Desktop.getDesktop().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
    }
  }
}
