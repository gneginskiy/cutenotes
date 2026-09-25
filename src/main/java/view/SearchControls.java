package view;

import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JPanel;

/** Trailing controls of the {@link SearchBar}: case toggle, previous / next and close. */
class SearchControls extends JPanel {

  private final FlatButton caseToggle;
  private final FlatButton prevButton;
  private final FlatButton nextButton;
  private final FlatButton closeButton;

  SearchControls(Runnable onPrev, Runnable onNext, Runnable onToggle, Runnable onClose) {
    super(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    setOpaque(false);
    caseToggle = new FlatButton("Aa", "Match case", () -> toggleCase(onToggle));
    prevButton =
        new FlatButton(VectorIcon.Kind.CHEVRON_UP, 14, "Previous match (Shift+Enter)", onPrev);
    nextButton = new FlatButton(VectorIcon.Kind.CHEVRON_DOWN, 14, "Next match (Enter)", onNext);
    closeButton = new FlatButton(VectorIcon.Kind.CLOSE, 14, "Close (Esc)", onClose);
    caseToggle.setFont(UiFonts.ui(Font.BOLD, 12f));
    add(caseToggle);
    add(prevButton);
    add(nextButton);
    add(closeButton);
  }

  private void toggleCase(Runnable onToggle) {
    caseToggle.setOn(!caseToggle.isOn());
    onToggle.run();
  }

  boolean isCaseSensitive() {
    return caseToggle.isOn();
  }

  void applyPalette(UiPalette p) {
    for (FlatButton b : new FlatButton[] {caseToggle, prevButton, nextButton, closeButton}) {
      b.applyPalette(p);
    }
  }
}
