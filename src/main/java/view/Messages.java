package view;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * The texts of the user interface, from {@code i18n/messages*.properties}. English and Russian are
 * available; {@code system} follows the language of the operating system.
 */
public final class Messages {

  /** Language settings offered in Options, in display order. */
  public static final List<String> LANGUAGES = List.of("system", "en", "ru");

  private static final String BUNDLE = "i18n.messages";

  /** Without this, a missing English bundle would fall back to the JVM's default locale. */
  private static final ResourceBundle.Control NO_FALLBACK =
      ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

  private static Locale locale = localeFor("system");
  private static ResourceBundle bundle = load(locale);

  private Messages() {}

  /** Switches the language; takes effect for texts created afterwards. */
  public static void useLanguage(String setting) {
    locale = localeFor(setting);
    bundle = load(locale);
  }

  public static Locale locale() {
    return locale;
  }

  /** The text for {@code key}, with {@code {0}}-style arguments filled in; the key if missing. */
  public static String tr(String key, Object... args) {
    String pattern;
    try {
      pattern = bundle.getString(key);
    } catch (MissingResourceException e) {
      return key;
    }
    return args.length == 0 ? pattern : new MessageFormat(pattern, locale).format(args);
  }

  static Locale localeFor(String setting) {
    String language =
        setting == null || "system".equals(setting) ? Locale.getDefault().getLanguage() : setting;
    return "ru".equals(language) ? Locale.forLanguageTag("ru") : Locale.ENGLISH;
  }

  static Set<String> keys(Locale forLocale) {
    return Collections.unmodifiableSet(load(forLocale).keySet());
  }

  private static ResourceBundle load(Locale forLocale) {
    return ResourceBundle.getBundle(BUNDLE, forLocale, NO_FALLBACK);
  }
}
