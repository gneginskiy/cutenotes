package dao;

import java.util.List;

public interface SessionStore {

  List<String> read();

  void write(List<String> tabNames);
}
