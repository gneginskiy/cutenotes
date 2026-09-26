package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

/** Screen readers announce icon buttons and tabs by name. */
class AccessibleNamesTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @Test
  void iconButtonsAndTabsHaveNames() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          FlatButton icon = new FlatButton(VectorIcon.Kind.PLUS, 12, "New tab", () -> {});
          FlatButton text = new FlatButton("Open", "Opens it", () -> {});
          TabChip chip = new TabChip("Groceries", () -> {});

          assertEquals("New tab", icon.getAccessibleContext().getAccessibleName());
          assertEquals("Open", text.getAccessibleContext().getAccessibleName());
          assertEquals("Groceries", chip.getAccessibleContext().getAccessibleName());
          chip.setTitle("Shopping");
          assertEquals("Shopping", chip.getAccessibleContext().getAccessibleName());
        });
  }
}
