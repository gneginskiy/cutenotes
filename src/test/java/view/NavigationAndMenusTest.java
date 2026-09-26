package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class NavigationAndMenusTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @Test
  void tabsAreReachedByNeighbourAndByNumber() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("a", "A", "1");
          tabs.openTab("b", "B", "2");
          tabs.openTab("c", "C", "3");

          tabs.selectRelative(1);
          assertEquals("a", tabs.activeId(), "wraps to the first");
          tabs.selectRelative(-1);
          assertEquals("c", tabs.activeId(), "wraps to the last");
          tabs.selectPosition(1);
          assertEquals("b", tabs.activeId());
          tabs.selectPosition(-1);
          assertEquals("c", tabs.activeId(), "9 means the last tab");
          tabs.selectPosition(7);
          assertEquals("c", tabs.activeId());
        });
  }

  @Test
  void closedTabsAreRememberedForReopening() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("a", "A", "1");
          tabs.openTab("b", "B", "2");
          tabs.close("a");
          tabs.close("b");
          assertEquals("b", tabs.popLastClosed());
          tabs.openTab("a", "A", "1");
          assertEquals(null, tabs.popLastClosed(), "already open again");
        });
  }

  @Test
  void menuItemsRunTheEditorsOwnShortcuts() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("a", "A", "word");
          tabs.activeArea().selectAll();

          JMenu format = EditMenus.format(tabs, ShortcutMask.menu());
          format.getItem(0).doClick();

          assertEquals("**word**", tabs.activeArea().markdown());
          assertTrue(
              EditorKeys.fire(
                  tabs.activeArea(), KeyStroke.getKeyStroke(KeyEvent.VK_Z, ShortcutMask.menu())));
          assertEquals("word", tabs.activeArea().markdown(), "undo through the same binding");
          assertEquals(
              false,
              EditorKeys.fire(tabs.activeArea(), KeyStroke.getKeyStroke(KeyEvent.VK_F13, 0)));
        });
  }

  @Test
  void colourMenuShowsTheCurrentColourAndSetsANewOne() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("a", "A", "x");
          TabHeader.Actions actions = tabs.strip().header().actions();
          actions.setColor().accept("a", "blue");
          JMenu menu =
              ColorMenu.build(actions.colorOf().apply("a"), c -> actions.setColor().accept("a", c));

          assertEquals(NoteColors.names().size() + 1, menu.getItemCount());
          assertTrue(((JRadioButtonMenuItem) menu.getItem(5)).isSelected(), "blue is ticked");
          menu.getItem(0).doClick();
          assertEquals("", tabs.identity().metas().get("a").color());
        });
  }

  @Test
  void helpMenuAndEmptyNoteHint() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          JMenu help = ViewHelpMenus.help(new HelpCommands(null, null));
          assertEquals("Keyboard shortcuts", help.getItem(0).getText());
          assertEquals(3, EmptyHint.lines(true).size());
          assertTrue(EmptyHint.lines(true).get(1).contains("⌘T"));
          assertTrue(EmptyHint.lines(false).get(1).contains("Ctrl+T"));
        });
  }
}
