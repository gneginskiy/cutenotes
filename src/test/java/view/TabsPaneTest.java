package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import model.Tab;

class TabsPaneTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void closingSoleTabFallsBackToDefault() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          TabsPane tabs = new TabsPane();
          tabs.openTab("X", "x", "hello");
          assertEquals(1, tabs.openIds().size());

          Tab closed = tabs.closeCurrent();

          assertNotNull(closed);
          assertEquals("X", closed.id());
          assertTrue(tabs.openIds().isEmpty());
          assertEquals(0, tabs.closeCurrent() == null ? 0 : 1, "second close should be a no-op");
        });
  }
}
