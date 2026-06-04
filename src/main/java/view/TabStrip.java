package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** The tabs bar plus a pin toggle that keeps it from auto-hiding. */
class TabStrip {

  private final JLabel pin = new JLabel("📌");
  private final JPanel pinHolder = new JPanel(new GridBagLayout());
  private final JPanel panel = new JPanel(new BorderLayout());
  private final transient Revealer revealer;
  private Color activeBg = Color.WHITE;
  private Color inactiveBg = Color.LIGHT_GRAY;

  TabStrip(JScrollPane headerScroll, JComponent layoutRoot, BooleanSupplier keepOpen) {
    pin.setOpaque(true);
    pin.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
    pin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    pinHolder.setOpaque(true);
    pinHolder.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    pinHolder.add(pin);
    headerScroll.addMouseWheelListener(
        e -> {
          var bar = headerScroll.getHorizontalScrollBar();
          bar.setValue(bar.getValue() + e.getUnitsToScroll() * bar.getUnitIncrement());
        });
    panel.setOpaque(true);
    panel.add(headerScroll, BorderLayout.CENTER);
    panel.add(pinHolder, BorderLayout.EAST);
    this.revealer = new Revealer(panel, layoutRoot, keepOpen);
    pin.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            revealer.setPinned(!revealer.isPinned());
            updatePin();
          }
        });
  }

  JComponent component() {
    return panel;
  }

  Revealer revealer() {
    return revealer;
  }

  boolean pinned() {
    return revealer.isPinned();
  }

  void applyTheme(Color bg, Color fg) {
    activeBg = bg;
    inactiveBg = Colors.contrast(bg, 0.12f);
    panel.setBackground(inactiveBg);
    pinHolder.setBackground(inactiveBg);
    pin.setForeground(fg);
    updatePin();
  }

  private void updatePin() {
    boolean on = revealer.isPinned();
    pin.setBackground(on ? activeBg : inactiveBg);
    pin.setToolTipText(on ? "Tabs bar pinned — click to unpin" : "Pin the tabs bar");
  }
}
