package view;

import static view.Messages.tr;

import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.JPanel;

/** Trailing controls of the {@link SearchBar}: match options, previous / next and close. */
class SearchControls extends JPanel {

  private final FlatButton caseToggle;
  private final FlatButton wordToggle;
  private final FlatButton regexToggle;
  private final List<FlatButton> buttons;

  SearchControls(Runnable onPrev, Runnable onNext, Runnable onToggle, Runnable onClose) {
    super(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    setOpaque(false);
    caseToggle = toggle("Aa", tr("search.matchCase"), onToggle);
    wordToggle = toggle("W", tr("search.wholeWord"), onToggle);
    regexToggle = toggle(".*", tr("search.regex"), onToggle);
    FlatButton prev = new FlatButton(VectorIcon.Kind.CHEVRON_UP, 14, tr("search.previous"), onPrev);
    FlatButton next = new FlatButton(VectorIcon.Kind.CHEVRON_DOWN, 14, tr("search.next"), onNext);
    FlatButton close = new FlatButton(VectorIcon.Kind.CLOSE, 14, tr("search.close"), onClose);
    buttons = List.of(caseToggle, wordToggle, regexToggle, prev, next, close);
    buttons.forEach(this::add);
  }

  private FlatButton toggle(String label, String tooltip, Runnable onToggle) {
    FlatButton[] self = new FlatButton[1];
    self[0] =
        new FlatButton(
            label,
            tooltip,
            () -> {
              self[0].setOn(!self[0].isOn());
              onToggle.run();
            });
    self[0].setFont(UiFonts.ui(Font.BOLD, 12f));
    return self[0];
  }

  SearchOptions options() {
    return new SearchOptions(caseToggle.isOn(), wordToggle.isOn(), regexToggle.isOn());
  }

  boolean isCaseSensitive() {
    return caseToggle.isOn();
  }

  void applyPalette(UiPalette p) {
    buttons.forEach(b -> b.applyPalette(p));
  }
}
