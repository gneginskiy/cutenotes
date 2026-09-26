package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MessagesTest {

  private static final Path BUNDLES = Path.of("src/main/resources/i18n");

  @AfterEach
  void backToEnglish() {
    Messages.useLanguage("en");
  }

  @Test
  void englishAndRussianHaveExactlyTheSameKeys() throws Exception {
    Set<String> en = new TreeSet<>(load("messages.properties").stringPropertyNames());
    Set<String> ru = new TreeSet<>(load("messages_ru.properties").stringPropertyNames());

    Set<String> onlyEn = new TreeSet<>(en);
    onlyEn.removeAll(ru);
    Set<String> onlyRu = new TreeSet<>(ru);
    onlyRu.removeAll(en);
    assertTrue(onlyEn.isEmpty(), "missing in Russian: " + onlyEn);
    assertTrue(onlyRu.isEmpty(), "missing in English: " + onlyRu);
  }

  @Test
  void everyKeyUsedInTheCodeExists() throws Exception {
    Set<String> keys = load("messages.properties").stringPropertyNames();
    Pattern call = Pattern.compile("tr\\(\\s*\"([a-zA-Z][\\w.]*)\"\\s*[,)]");
    Set<String> missing = new TreeSet<>();
    try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
      for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
        Matcher m = call.matcher(Files.readString(file));
        while (m.find()) {
          if (!keys.contains(m.group(1))) {
            missing.add(file.getFileName() + ": " + m.group(1));
          }
        }
      }
    }
    for (String color : NoteColors.names()) {
      if (!keys.contains("tab.color." + color)) {
        missing.add("colour " + color);
      }
    }
    assertTrue(missing.isEmpty(), "unknown keys: " + missing);
  }

  @Test
  void everyPatternIsAValidMessageFormat() throws Exception {
    for (String bundle : new String[] {"messages.properties", "messages_ru.properties"}) {
      Properties p = load(bundle);
      for (String key : p.stringPropertyNames()) {
        String value = p.getProperty(key);
        if (value.contains("{0}")) {
          new MessageFormat(value).format(new Object[] {"x", "y"});
        }
        assertTrue(!value.contains("'") || !value.contains("{0}"), key + ": use ’ with arguments");
      }
    }
  }

  @Test
  void languageSettingSelectsTheBundleWithoutFallingBackToTheSystem() {
    Messages.useLanguage("ru");
    assertEquals("Файл", Messages.tr("menu.file"));
    assertEquals("5 мин назад", Messages.tr("time.minutesAgo", 5));

    Messages.useLanguage("en");
    assertEquals("File", Messages.tr("menu.file"));
    assertEquals(Locale.ENGLISH, Messages.locale());
  }

  @Test
  void unknownKeysAndLanguagesDegradeGracefully() {
    assertEquals("no.such.key", Messages.tr("no.such.key"));
    assertEquals(Locale.ENGLISH, Messages.localeFor("de"));
    assertEquals("ru", Messages.localeFor("ru").getLanguage());
  }

  private static Properties load(String name) throws Exception {
    Properties p = new Properties();
    try (Reader r = Files.newBufferedReader(BUNDLES.resolve(name), StandardCharsets.UTF_8)) {
      p.load(r);
    }
    return p;
  }
}
