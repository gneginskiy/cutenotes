package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

/**
 * A budget for large notes: a 1 MB note opens, reads back and exports in well under the limit. The
 * limit is generous (slow CI machines); it catches accidental quadratic work, not small slowdowns.
 */
class LargeNoteTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  private static final long BUDGET_MS = 10_000;

  @Test
  void aMegabyteNoteOpensAndSavesWithinBudget() throws Exception {
    StringBuilder md = new StringBuilder();
    for (int i = 0; md.length() < 1_000_000; i++) {
      md.append("- [ ] item ")
          .append(i)
          .append(" with **bold** and https://example.com/")
          .append(i);
      md.append('\n');
    }
    String text = md.toString();
    long[] took = new long[1];

    SwingUtilities.invokeAndWait(
        () -> {
          long start = System.nanoTime();
          NoteEditor editor = new NoteEditor(text);
          String back = editor.markdown();
          took[0] = (System.nanoTime() - start) / 1_000_000;
          assertEquals(text, back);
        });

    assertTrue(took[0] < BUDGET_MS, "opening and saving 1 MB took " + took[0] + " ms");
  }
}
