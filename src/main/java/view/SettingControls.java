package view;

import java.awt.Component;
import java.util.List;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import model.AppSettings;

/** The controls of {@link AppSettingsPanel}: each change is saved at once, then followed up. */
final class SettingControls {

  private SettingControls() {}

  /** A drop-down of {@code values} shown by {@code label}; picking one runs {@code onPick}. */
  static <T> JComboBox<T> choice(
      List<T> values, T selected, Function<T, String> label, Consumer<T> onPick) {
    JComboBox<T> box = new JComboBox<>(new Vector<>(values));
    box.setSelectedItem(selected);
    box.setRenderer(
        new DefaultListCellRenderer() {
          @Override
          @SuppressWarnings("unchecked")
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean sel, boolean focus) {
            String text = value == null ? "" : label.apply((T) value);
            return super.getListCellRendererComponent(list, text, index, sel, focus);
          }
        });
    box.addActionListener(e -> onPick.accept(box.getItemAt(box.getSelectedIndex())));
    return box;
  }

  static JCheckBox check(
      String label,
      boolean selected,
      Runnable after,
      BiFunction<AppSettings, Boolean, AppSettings> with) {
    JCheckBox box = new JCheckBox(label, selected);
    box.addActionListener(e -> apply(x -> with.apply(x, box.isSelected()), after));
    return box;
  }

  static void apply(UnaryOperator<AppSettings> change, Runnable then) {
    SettingsHolder.update(change);
    then.run();
  }

  static JComponent row(String label, JComponent field) {
    Box row = OptionsForm.row(new JLabel(label + "  "), field);
    row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
    return OptionsForm.left(row);
  }
}
