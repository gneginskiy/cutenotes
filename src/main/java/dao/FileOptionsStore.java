package dao;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import model.Theme;

public class FileOptionsStore implements OptionsStore {

  private static final String BG = "backgroundColor";
  private static final String FG = "textColor";
  private static final String CARET = "caretColor";
  private static final String FONT = "fontFamily";
  private static final String SIZE = "fontSize";
  private static final String TITLE = "windowTitle";
  private static final String ALWAYS_ON_TOP = "alwaysOnTop";

  private final Path file;

  public FileOptionsStore() {
    this(DataDir.resolve().resolve("cutenotes_options.txt"));
  }

  public FileOptionsStore(Path file) {
    this.file = file;
  }

  @Override
  @SneakyThrows
  public Theme load() {
    if (!Files.exists(file)) {
      return Theme.DEFAULT;
    }
    Map<String, String> m = parse(Files.readString(file, StandardCharsets.UTF_8));
    return new Theme(
        parseColor(m.get(BG), Theme.DEFAULT.bg()),
        parseColor(m.get(FG), Theme.DEFAULT.fg()),
        m.containsKey(CARET) ? parseColor(m.get(CARET), null) : null,
        m.getOrDefault(FONT, Theme.DEFAULT.fontFamily()),
        parseInt(m.get(SIZE), Theme.DEFAULT.fontSize()),
        m.get(TITLE),
        !"false".equalsIgnoreCase(m.get(ALWAYS_ON_TOP)));
  }

  @Override
  @SneakyThrows
  public void save(Theme t) {
    Files.createDirectories(file.getParent());
    StringBuilder text = new StringBuilder();
    text.append(BG).append('=').append(toHex(t.bg())).append('\n');
    text.append(FG).append('=').append(toHex(t.fg())).append('\n');
    if (t.caret() != null) {
      text.append(CARET).append('=').append(toHex(t.caret())).append('\n');
    }
    text.append(FONT).append('=').append(t.fontFamily()).append('\n');
    text.append(SIZE).append('=').append(t.fontSize()).append('\n');
    if (t.title() != null && !t.title().isBlank()) {
      text.append(TITLE).append('=').append(t.title()).append('\n');
    }
    text.append(ALWAYS_ON_TOP).append('=').append(t.alwaysOnTop()).append('\n');
    Files.writeString(file, text.toString(), StandardCharsets.UTF_8);
  }

  private static Map<String, String> parse(String content) {
    Map<String, String> m = new HashMap<>();
    for (String line : content.split("\\R")) {
      int eq = line.indexOf('=');
      if (eq > 0) {
        m.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
      }
    }
    return m;
  }

  @SuppressWarnings("PMD")
  private static Color parseColor(String hex, Color fallback) {
    if (hex == null) {
      return fallback;
    }
    try {
      return Color.decode(hex);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static int parseInt(String s, int fallback) {
    if (s == null) {
      return fallback;
    }
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static String toHex(Color c) {
    return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
  }
}
