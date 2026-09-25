package dao;

import java.nio.charset.StandardCharsets;

/**
 * The on-disk naming scheme of notes: {@code note_<id>_<name>.txt}. The name part is only a hint
 * for humans browsing the folder (the real name is the file's first line), so it is sanitised and
 * capped well below the 255-byte file-name limit of common file systems.
 */
final class NoteFileNames {

  static final int MAX_NAME_BYTES = 120;

  private static final String PREFIX = "note_";
  private static final String SUFFIX = ".txt";
  private static final String SEP = "_";

  private NoteFileNames() {}

  static String of(String id, String name) {
    return PREFIX + id + SEP + namePart(name) + SUFFIX;
  }

  static boolean isNote(String fileName) {
    return fileName.startsWith(PREFIX) && fileName.endsWith(SUFFIX);
  }

  static boolean matchesId(String fileName, String id) {
    String prefix = PREFIX + id;
    if (!fileName.startsWith(prefix) || !fileName.endsWith(SUFFIX)) {
      return false;
    }
    String rest = fileName.substring(prefix.length());
    return rest.equals(SUFFIX) || rest.startsWith(SEP);
  }

  /**
   * The id is whatever precedes {@code _<name part>}; ids may themselves contain {@code _} (e.g.
   * the default scratch note {@code __default__}), so splitting on the first one is wrong.
   */
  static String idOf(String fileName, String name) {
    String stripped = fileName.substring(PREFIX.length(), fileName.length() - SUFFIX.length());
    String suffix = SEP + namePart(name);
    if (stripped.length() > suffix.length() && stripped.endsWith(suffix)) {
      return stripped.substring(0, stripped.length() - suffix.length());
    }
    int sep = stripped.indexOf(SEP);
    return sep < 0 ? stripped : stripped.substring(0, sep);
  }

  static String namePart(String name) {
    String safe = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", SEP);
    int bytes = 0;
    int end = 0;
    while (end < safe.length()) {
      int cp = safe.codePointAt(end);
      bytes += Character.toString(cp).getBytes(StandardCharsets.UTF_8).length;
      if (bytes > MAX_NAME_BYTES) {
        break;
      }
      end += Character.charCount(cp);
    }
    return safe.substring(0, end);
  }
}
