package view;

import java.awt.Color;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

final class ColorField extends Box {

  private final JRadioButton radio;
  private final JTextField hex = new JTextField(8);

  ColorField(String label, boolean selected) {
    super(BoxLayout.Y_AXIS);
    radio = new JRadioButton(label, selected);
    radio.setAlignmentX(LEFT_ALIGNMENT);
    hex.setAlignmentX(LEFT_ALIGNMENT);
    add(radio);
    add(hex);
  }

  JRadioButton radio() {
    return radio;
  }

  boolean isSelected() {
    return radio.isSelected();
  }

  void show(Color c) {
    hex.setText(c == null ? "" : Colors.toHex(c));
  }

  void onTyped(Consumer<Color> handler) {
    hex.addActionListener(e -> tryParse(handler));
  }

  private void tryParse(Consumer<Color> handler) {
    String t = hex.getText().trim();
    if (t.isEmpty()) {
      handler.accept(null);
      return;
    }
    try {
      handler.accept(Color.decode(t));
    } catch (NumberFormatException ignored) {
      // keep current value on bad input
    }
  }
}
