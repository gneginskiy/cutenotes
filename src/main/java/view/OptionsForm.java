package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Lays out the Options dialog: themes, live preview, colours, text and window settings. */
final class OptionsForm {

  record Actions(Runnable reset, Runnable cancel, Runnable ok) {}

  record Colors(ColorField bg, ColorField fg, ColorField caret, ColorField code) {}

  private static final Color CAPTION = new Color(128, 128, 128);

  private OptionsForm() {}

  static JPanel build(
      JComponent presets,
      JComponent preview,
      Colors colors,
      JColorChooser chooser,
      OptionsFields fields,
      Actions actions) {
    JPanel root = new JPanel(new BorderLayout(0, 12));
    root.setBorder(BorderFactory.createEmptyBorder(16, 16, 14, 16));
    root.add(buildTop(presets, preview), BorderLayout.NORTH);
    root.add(buildColors(colors, chooser), BorderLayout.CENTER);
    root.add(buildBottom(fields, actions), BorderLayout.SOUTH);
    return root;
  }

  private static JPanel buildTop(JComponent presets, JComponent preview) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(left(caption("Theme")));
    p.add(left(presets));
    JLabel previewCaption = caption("Preview");
    previewCaption.setBorder(BorderFactory.createEmptyBorder(12, 0, 6, 0));
    p.add(left(previewCaption));
    JScrollPane scroller = new JScrollPane(preview);
    scroller.setPreferredSize(new Dimension(0, 96));
    p.add(left(scroller));
    return p;
  }

  private static JPanel buildColors(Colors colors, JColorChooser chooser) {
    JPanel p = new JPanel(new BorderLayout(0, 6));
    Box row = Box.createHorizontalBox();
    row.add(colors.bg());
    row.add(Box.createHorizontalStrut(10));
    row.add(colors.fg());
    row.add(Box.createHorizontalStrut(10));
    row.add(colors.caret());
    row.add(Box.createHorizontalStrut(10));
    row.add(colors.code());
    row.add(Box.createHorizontalGlue());
    Box top = Box.createVerticalBox();
    top.add(left(caption("Colours")));
    top.add(left(row));
    p.add(top, BorderLayout.NORTH);
    p.add(chooser, BorderLayout.CENTER);
    return p;
  }

  private static JPanel buildBottom(OptionsFields f, Actions actions) {
    Box settings = Box.createVerticalBox();
    settings.add(left(caption("Text & window")));
    settings.add(left(row(new JLabel("Font "), f.fontBox(), new JLabel("  Size "), f.sizeSpin())));
    settings.add(Box.createVerticalStrut(6));
    settings.add(left(row(new JLabel("Window title "), f.titleField())));
    settings.add(Box.createVerticalStrut(4));
    settings.add(left(row(f.alwaysOnTop())));
    Box buttons = Box.createHorizontalBox();
    buttons.add(btn("Reset to default", actions.reset()));
    buttons.add(Box.createHorizontalGlue());
    buttons.add(btn("Cancel", actions.cancel()));
    buttons.add(btn("OK", actions.ok()));
    JPanel p = new JPanel(new BorderLayout(0, 12));
    p.add(settings, BorderLayout.NORTH);
    p.add(buttons, BorderLayout.SOUTH);
    return p;
  }

  private static JLabel caption(String text) {
    JLabel l = new JLabel(text);
    l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
    l.setForeground(CAPTION);
    l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
    return l;
  }

  private static Box row(JComponent... parts) {
    Box b = Box.createHorizontalBox();
    for (JComponent part : parts) {
      b.add(part);
    }
    b.add(Box.createHorizontalGlue());
    return b;
  }

  private static <T extends JComponent> T left(T c) {
    c.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    return c;
  }

  private static JButton btn(String label, Runnable action) {
    JButton b = new JButton(label);
    b.addActionListener(e -> action.run());
    return b;
  }
}
