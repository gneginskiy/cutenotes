package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Trailing controls of the {@link SearchBar}: match counter, case toggle and navigation. */
class SearchControls extends JPanel {

  private final JLabel counter = new JLabel();
  private final JToggleButton caseToggle = new JToggleButton("Aa");
  private final JButton prevButton = new JButton("˄");
  private final JButton nextButton = new JButton("˅");
  private final JButton closeButton = new JButton("✕");

  SearchControls(Runnable onPrev, Runnable onNext, Runnable onToggle, Runnable onClose) {
    super(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    setOpaque(false);
    counter.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 6));
    prevButton.setToolTipText("Previous match (Shift+Enter)");
    nextButton.setToolTipText("Next match (Enter)");
    caseToggle.setToolTipText("Match case");
    closeButton.setToolTipText("Close (Esc)");
    for (AbstractButton b : buttons()) {
      b.setFocusable(false);
      b.setMargin(new Insets(1, 2, 1, 2));
      b.putClientProperty("JButton.buttonType", "square");
      b.setPreferredSize(new Dimension(42, 24));
    }
    prevButton.addActionListener(e -> onPrev.run());
    nextButton.addActionListener(e -> onNext.run());
    caseToggle.addActionListener(e -> onToggle.run());
    closeButton.addActionListener(e -> onClose.run());
    add(counter);
    add(caseToggle);
    add(prevButton);
    add(nextButton);
    add(closeButton);
  }

  boolean isCaseSensitive() {
    return caseToggle.isSelected();
  }

  void setCounterText(String text) {
    counter.setText(text);
  }

  void applyForeground(Color fg) {
    counter.setForeground(fg);
    for (AbstractButton b : buttons()) {
      b.setForeground(fg);
    }
  }

  private AbstractButton[] buttons() {
    return new AbstractButton[] {caseToggle, prevButton, nextButton, closeButton};
  }
}
