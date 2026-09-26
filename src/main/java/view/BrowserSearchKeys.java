package view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Keys of the notes browser's search box: Enter opens, ↓ moves into the list, Esc clears. */
final class BrowserSearchKeys extends KeyAdapter {

  private final GroupTree tree;
  private final HintField input;
  private final Runnable openBest;

  BrowserSearchKeys(GroupTree tree, HintField input, Runnable openBest) {
    this.tree = tree;
    this.input = input;
    this.openBest = openBest;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_ENTER -> openBest.run();
      case KeyEvent.VK_DOWN -> {
        if (tree.selectedNote() == null) {
          tree.selectFirstNote();
        }
        tree.requestFocusInWindow();
      }
      case KeyEvent.VK_ESCAPE -> {
        if (!input.getText().isEmpty()) {
          input.setText("");
          e.consume();
        }
      }
      default -> {
        // other keys edit the query
      }
    }
  }
}
