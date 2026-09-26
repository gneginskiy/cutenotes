package view;

import java.util.function.UnaryOperator;

import dao.SettingsStore;
import model.AppSettings;

/** The application settings in effect (see {@link AppSettings}) and where they are saved. */
public final class SettingsHolder {

  private static AppSettings current = AppSettings.DEFAULT;
  private static SettingsStore store;

  private SettingsHolder() {}

  public static AppSettings current() {
    return current;
  }

  public static void set(AppSettings settings) {
    current = settings;
  }

  /** Loads the saved settings; later changes are written back to {@code settingsStore}. */
  public static void init(SettingsStore settingsStore) {
    store = settingsStore;
    current = settingsStore.load();
  }

  /** Applies a change and saves it (when a store is attached). */
  static void update(UnaryOperator<AppSettings> change) {
    current = change.apply(current);
    if (store != null) {
      store.save(current);
    }
  }
}
