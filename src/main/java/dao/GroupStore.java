package dao;

import model.GroupData;

public interface GroupStore {

  GroupData read();

  void write(GroupData data);
}
