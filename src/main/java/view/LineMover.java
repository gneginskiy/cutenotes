package view;

import java.awt.Point;
import java.util.List;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;

/**
 * Carries out a {@link LineMove} in an editor the way IntelliJ does: one undo step, the selection
 * travels with the lines, and the view stays put unless the moved lines would leave it. The caret
 * is frozen while the document changes, so intermediate positions never scroll the view.
 */
final class LineMover {

  private LineMover() {}

  static void move(JTextComponent area, EditorUndo undo, int direction) {
    Caret caret = area.getCaret();
    int dot = caret.getDot();
    int mark = caret.getMark();
    LineMove plan =
        LineMove.plan(LineOps.docText(area), Math.min(dot, mark), Math.max(dot, mark), direction);
    if (plan == null) {
      return;
    }
    JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, area);
    Point view = viewport == null ? null : viewport.getViewPosition();
    int policy = freeze(caret);
    undo.beginGroup();
    try {
      apply((StyledDocument) area.getDocument(), plan);
    } finally {
      undo.endGroup();
      unfreeze(caret, policy);
    }
    if (viewport != null) {
      viewport.setViewPosition(view);
    }
    CaretHistory.quietly(
        area,
        () -> {
          area.setCaretPosition(plan.moved(mark));
          area.moveCaretPosition(plan.moved(dot));
        });
  }

  /** Copies the neighbour line to the other side of the block, then removes the original. */
  @SneakyThrows
  private static void apply(StyledDocument doc, LineMove plan) {
    List<StyledRuns.Run> runs = StyledRuns.capture(doc, plan.copyFrom(), plan.copyTo());
    int at = plan.insertAt();
    if (plan.newlineBefore()) {
      doc.insertString(at, "\n", null);
      at++;
    }
    at = StyledRuns.insert(doc, at, runs);
    if (plan.newlineAfter()) {
      doc.insertString(at, "\n", null);
    }
    doc.remove(plan.removeFrom(), plan.removeTo() - plan.removeFrom());
  }

  private static int freeze(Caret caret) {
    if (caret instanceof DefaultCaret dc) {
      int policy = dc.getUpdatePolicy();
      dc.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
      return policy;
    }
    return -1;
  }

  private static void unfreeze(Caret caret, int policy) {
    if (caret instanceof DefaultCaret dc && policy >= 0) {
      dc.setUpdatePolicy(policy);
    }
  }
}
