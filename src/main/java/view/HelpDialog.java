package view;

import static view.Messages.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** The keyboard shortcuts sheet (F1), in two columns and the colours of the current theme. */
class HelpDialog extends JDialog {

  private static final float MIN_FONT = 9f;
  private static final double SCREEN_FILL = 0.92;

  HelpDialog(JFrame owner) {
    super(owner, tr("help.title"), false);
    JPanel content = buildContent(UiPalette.current());
    setContentPane(content);
    Dialogs.bindEscape(this);
    pack();
    fitToScreen(content);
    WindowPlacement.centerOnOwner(this);
  }

  private void fitToScreen(JPanel content) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int maxH = (int) (screen.height * SCREEN_FILL);
    int maxW = (int) (screen.width * SCREEN_FILL);
    if (getHeight() <= maxH && getWidth() <= maxW) {
      return;
    }
    float scale = Math.min((float) maxH / getHeight(), (float) maxW / getWidth());
    scaleFonts(content, scale);
    pack();
  }

  private static void scaleFonts(Container c, float scale) {
    for (Component child : c.getComponents()) {
      Font f = child.getFont();
      if (f != null) {
        child.setFont(f.deriveFont(Math.max(MIN_FONT, f.getSize2D() * scale)));
      }
      if (child instanceof Container nested) {
        scaleFonts(nested, scale);
      }
    }
  }

  private static JPanel buildContent(UiPalette p) {
    boolean mac = PlatformLook.MAC;
    JPanel columns = new JPanel(new GridLayout(1, 2, 40, 0));
    columns.setOpaque(false);
    columns.add(
        new HelpSections(p, mac)
            .section(tr("help.section.tabs"), HelpRows.tabs(mac))
            .section(tr("help.section.formatting"), HelpRows.formatting(mac))
            .end()
            .panel());
    columns.add(
        new HelpSections(p, mac)
            .section(tr("help.section.editing"), HelpRows.editing(mac))
            .section(tr("help.section.display"), HelpRows.display(mac))
            .end()
            .panel());
    JPanel root = new JPanel(new BorderLayout(0, 20));
    root.setBackground(p.bg());
    root.setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
    root.add(title(p), BorderLayout.NORTH);
    root.add(columns, BorderLayout.CENTER);
    return root;
  }

  private static JPanel title(UiPalette p) {
    JLabel title = new JLabel(tr("help.title"));
    title.setFont(UiFonts.ui(Font.BOLD, 20f));
    title.setForeground(p.fg());
    JLabel hint = new JLabel(tr("help.hint"));
    hint.setFont(UiFonts.ui(Font.PLAIN, 12f));
    hint.setForeground(p.muted());
    JPanel box = new JPanel(new BorderLayout(0, 2));
    box.setOpaque(false);
    box.add(title, BorderLayout.NORTH);
    box.add(hint, BorderLayout.SOUTH);
    return box;
  }
}
