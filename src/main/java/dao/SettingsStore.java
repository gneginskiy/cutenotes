package dao;

import model.AppSettings;

public interface SettingsStore {

  AppSettings load();

  void save(AppSettings settings);
}
