package view;

import static view.Messages.tr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** The second row of the find bar: the replacement text and the two replace buttons. */
final class ReplaceRow extends JPanel {

  private final SearchField field = new SearchField(tr("search.replace"));
  private final List<FlatButton> buttons;

  ReplaceRow(Runnable replaceOne, Runnable replaceAll) {
    super(new BorderLayout(8, 0));
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
    FlatButton one = new FlatButton(tr("search.replaceOne"), "Enter", replaceOne);
    FlatButton all =
        new FlatButton(
            tr("search.replaceAll"), PlatformKeys.menuKey(PlatformLook.MAC) + "+Enter", replaceAll);
    buttons = List.of(one, all);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    actions.setOpaque(false);
    buttons.forEach(
        b -> {
          b.setFont(UiFonts.ui(Font.PLAIN, 12f));
          actions.add(b);
        });
    add(field, BorderLayout.CENTER);
    add(actions, BorderLayout.EAST);
    setVisible(false);
  }

  HintField input() {
    return field.input();
  }

  void applyPalette(UiPalette p) {
    field.applyPalette(p);
    buttons.forEach(b -> b.applyPalette(p));
  }
}
