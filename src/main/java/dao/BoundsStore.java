package dao;

import java.awt.Rectangle;

/** Remembers where the main window was, so it reopens exactly where the user left it. */
public interface BoundsStore {

  /** The last saved bounds, or {@code null} when nothing usable was saved. */
  Rectangle load();

  void save(Rectangle bounds);
}
