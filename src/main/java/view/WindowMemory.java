package view;

import java.awt.Dimension;
import java.awt.Frame;

import dao.BoundsStore;
import dao.FileBoundsStore;

/** Puts the main window back where the user left it last time. */
final class WindowMemory {

  static final Dimension DEFAULT_SIZE = new Dimension(560, 440);

  private final BoundsStore store;

  WindowMemory() {
    this(new FileBoundsStore());
  }

  WindowMemory(BoundsStore store) {
    this.store = store;
  }

  void restore(Frame frame) {
    WindowPlacement.restore(frame, store.load(), DEFAULT_SIZE);
  }

  /** A maximised window keeps the bounds it had before, so un-maximising still makes sense. */
  void save(Frame frame) {
    if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
      store.save(frame.getBounds());
    }
  }
}
