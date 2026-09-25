package view;

import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.Tab;

/** The card stack of tab editors, plus the default scratch editor shown when no tab is open. */
final class TabCards {

  private final CardLayout cards = new CardLayout();
  private final JPanel panel = new JPanel(cards);
  private final NoteEditor defaultArea = new NoteEditor("");

  TabCards() {
    panel.setBackground(ThemeHolder.current().bg());
    panel.add(TabPanes.content(defaultArea), Tab.DEFAULT_ID);
    cards.show(panel, Tab.DEFAULT_ID);
  }

  JPanel component() {
    return panel;
  }

  NoteEditor defaultArea() {
    return defaultArea;
  }

  void add(JScrollPane pane, String id) {
    panel.add(pane, id);
  }

  void remove(JScrollPane pane) {
    panel.remove(pane);
  }

  void show(String id) {
    cards.show(panel, id);
  }

  void showDefault() {
    cards.show(panel, Tab.DEFAULT_ID);
    TabPanes.focusLater(defaultArea);
  }
}
