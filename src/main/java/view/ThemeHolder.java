package view;

import model.Theme;

public final class ThemeHolder {

  private static Theme current = Theme.DEFAULT;

  private ThemeHolder() {}

  public static Theme current() {
    return current;
  }

  public static void set(Theme theme) {
    current = theme;
  }
}
