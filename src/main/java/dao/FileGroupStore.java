package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import model.Group;
import model.GroupData;

/**
 * Persists {@link GroupData} as a tab-separated text file. {@code G<TAB>id<TAB>parentId<TAB>name}
 * lines declare groups (empty {@code parentId} means top level); {@code N<TAB>noteId<TAB>groupId}
 * lines assign a note to a group.
 */
public class FileGroupStore implements GroupStore {

  private static final String FILE_NAME = "cutenotes_groups.txt";
  private static final String GROUP = "G";
  private static final String NOTE = "N";
  private static final String SEP = "\t";

  private final Path file;

  public FileGroupStore() {
    this(DataDir.resolve().resolve(FILE_NAME));
  }

  public FileGroupStore(Path file) {
    this.file = file;
  }

  @Override
  @SneakyThrows
  public GroupData read() {
    if (!Files.exists(file)) {
      return GroupData.EMPTY;
    }
    List<Group> groups = new ArrayList<>();
    Map<String, String> assignments = new LinkedHashMap<>();
    for (String line : Files.readString(file, StandardCharsets.UTF_8).split("\\R")) {
      String[] parts = line.split(SEP, -1);
      if (parts.length >= 4 && GROUP.equals(parts[0])) {
        groups.add(new Group(parts[1], parts[3], parts[2].isEmpty() ? null : parts[2]));
      } else if (parts.length >= 3 && NOTE.equals(parts[0])) {
        assignments.put(parts[1], parts[2]);
      }
    }
    return new GroupData(groups, assignments);
  }

  @Override
  @SneakyThrows
  public void write(GroupData data) {
    Files.createDirectories(file.getParent());
    StringBuilder text = new StringBuilder();
    for (Group group : data.groups()) {
      text.append(GROUP)
          .append(SEP)
          .append(group.id())
          .append(SEP)
          .append(group.parentId() == null ? "" : group.parentId())
          .append(SEP)
          .append(group.name())
          .append('\n');
    }
    for (Map.Entry<String, String> entry : data.assignments().entrySet()) {
      text.append(NOTE).append(SEP).append(entry.getKey()).append(SEP).append(entry.getValue());
      text.append('\n');
    }
    Files.writeString(file, text.toString(), StandardCharsets.UTF_8);
  }
}
