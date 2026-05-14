package view;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

final class Spinners {

  private Spinners() {}

  static JSpinner intRange(int initial, int min, int max) {
    return new JSpinner(new SpinnerNumberModel(initial, min, max, 1));
  }
}
