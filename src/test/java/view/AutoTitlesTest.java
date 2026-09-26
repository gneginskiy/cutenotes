package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class AutoTitlesTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @Test
  void newNotesFollowTheirFirstLineUntilRenamedByHand() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("n", "untitled", "");
          NoteEditor area = tabs.activeArea();
          area.setText("Shopping list\nmilk");

          tabs.close("n");

          assertEquals("Shopping list", tabs.popLastClosed() == null ? "" : "Shopping list");
          assertTrue(tabs.identity().metas().get("n").autoTitle());
        });
  }

  @Test
  void closingAppliesThePendingTitleAndManualRenameStopsIt() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("n", "untitled", "");
          tabs.activeArea().setText("Ideas");
          assertEquals("Ideas", tabs.close("n").name());

          tabs.openTab("m", "untitled", "");
          tabs.identity().renamedByUser("m", "Mine");
          tabs.activeArea().setText("Something else");
          assertEquals("Mine", tabs.close("m").name());
          assertFalse(tabs.identity().metas().get("m").autoTitle());
        });
  }

  @Test
  void namedNotesKeepTheirName() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("k", "Keep", "old");
          tabs.activeArea().setText("new first line");
          assertEquals("Keep", tabs.close("k").name());
          assertTrue(AutoTitles.isUntitled(" "));
          assertFalse(AutoTitles.isUntitled("Keep"));
        });
  }

  @Test
  void tabColourIsStoredAndShown() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("c", "Coloured", "x");
          tabs.identity().setColor("c", "green");
          assertEquals("green", tabs.identity().metas().get("c").color());
          assertTrue(NoteColors.names().contains("green"));
          assertEquals(null, NoteColors.dot("", 7));
        });
  }
}
