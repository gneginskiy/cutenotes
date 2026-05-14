package dao;

import java.util.List;

import model.Tab;
import model.TabMeta;

public interface TabRepository {

  List<TabMeta> listMeta();

  Tab load(String id);

  void save(String id, String name, String content);

  void delete(String id);

  String newId();
}
