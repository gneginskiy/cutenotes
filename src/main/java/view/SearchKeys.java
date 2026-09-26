package view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Keys of the find bar's fields: Esc closes; in the find field Enter / Shift+Enter and ↓ / ↑ step
 * through the matches, in the replace field Enter replaces one and Cmd/Ctrl+Enter replaces all.
 */
final class SearchKeys extends KeyAdapter {

  private final SearchBar bar;
  private final boolean replacing;

  SearchKeys(SearchBar bar, boolean replacing) {
    this.bar = bar;
    this.replacing = replacing;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int c = e.getKeyCode();
    boolean all = (e.getModifiersEx() & ShortcutMask.menu()) != 0;
    if (c == KeyEvent.VK_ESCAPE) {
      bar.close();
      e.consume();
    } else if (c == KeyEvent.VK_ENTER && replacing) {
      if (all) {
        bar.replaceAll();
      } else {
        bar.replaceOne();
      }
    } else if (c == KeyEvent.VK_ENTER || c == KeyEvent.VK_DOWN || c == KeyEvent.VK_UP) {
      bar.step(c == KeyEvent.VK_UP || e.isShiftDown() ? -1 : 1);
    }
  }
}
