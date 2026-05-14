package dao;

import model.Theme;

public interface OptionsStore {

  Theme load();

  void save(Theme theme);
}
