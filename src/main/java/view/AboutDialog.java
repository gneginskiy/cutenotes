package view;

import static view.Messages.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dao.DataDir;
import util.AppVersion;

/**
 * "About cuteNotes": icon, version, the notes folder (with a button to open it) and the licence.
 */
class AboutDialog extends JDialog {

  static final String PROJECT_PAGE = "https://github.com/gneginskiy/cutenotes";

  AboutDialog(JFrame owner) {
    super(owner, tr("about.title"), false);
    UiPalette p = UiPalette.current();
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    root.setBackground(p.bg());
    root.setBorder(BorderFactory.createEmptyBorder(26, 34, 24, 34));
    root.add(centered(new JLabel(new ImageIcon(AppIcon.image(88)))));
    root.add(Box.createVerticalStrut(10));
    root.add(centered(label("cuteNotes", Font.BOLD, 22f, p.fg())));
    root.add(centered(label(tr("about.tagline"), Font.PLAIN, 13f, p.muted())));
    root.add(Box.createVerticalStrut(14));
    root.add(centered(label(tr("about.version", AppVersion.current()), Font.PLAIN, 13f, p.fg())));
    root.add(Box.createVerticalStrut(14));
    root.add(centered(label(tr("about.folder"), Font.BOLD, 11f, p.muted())));
    root.add(centered(label(DataDir.resolve().toString(), Font.PLAIN, 12f, p.fg())));
    root.add(centered(buttons(p)));
    root.add(Box.createVerticalStrut(12));
    root.add(centered(label(tr("about.license"), Font.PLAIN, 11f, p.muted())));
    setContentPane(root);
    Dialogs.bindEscape(this);
    pack();
    setResizable(false);
    WindowPlacement.centerOnOwner(this);
  }

  private static JPanel buttons(UiPalette p) {
    FlatButton open =
        new FlatButton(
            VectorIcon.Kind.FOLDER,
            tr("about.openFolder"),
            null,
            () -> Desktops.open(DataDir.resolve()));
    FlatButton site =
        new FlatButton(
            VectorIcon.Kind.NOTE,
            tr("about.website"),
            PROJECT_PAGE,
            () -> Desktops.browse(PROJECT_PAGE));
    open.applyPalette(p);
    site.applyPalette(p);
    JPanel row = new JPanel();
    row.setOpaque(false);
    row.add(open);
    row.add(site);
    return row;
  }

  private static JLabel label(String text, int style, float size, Color color) {
    JLabel l = new JLabel(text);
    l.setFont(UiFonts.ui(style, size));
    l.setForeground(color);
    return l;
  }

  private static <T extends JComponent> Component centered(T c) {
    c.setAlignmentX(Component.CENTER_ALIGNMENT);
    return c;
  }
}
