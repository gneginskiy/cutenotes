package view;

import static view.Messages.tr;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Human dates for note lists: "just now", "5 min ago", "14:05", "Yesterday", "Mon", "12 Sep". */
final class RelativeTime {

  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter WEEKDAY =
      DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);
  private static final DateTimeFormatter DAY_MONTH =
      DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
  private static final DateTimeFormatter FULL =
      DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

  private RelativeTime() {}

  static String format(Instant then, Instant now, ZoneId zone) {
    Duration age = Duration.between(then, now);
    if (age.isNegative() || age.toSeconds() < 60) {
      return tr("time.justNow");
    }
    if (age.toMinutes() < 60) {
      return tr("time.minutesAgo", age.toMinutes());
    }
    ZonedDateTime at = then.atZone(zone);
    LocalDate today = now.atZone(zone).toLocalDate();
    long days = ChronoUnit.DAYS.between(at.toLocalDate(), today);
    if (days == 0) {
      return at.format(TIME);
    }
    if (days == 1) {
      return tr("time.yesterday");
    }
    if (days < 7) {
      return capitalize(at.format(WEEKDAY.withLocale(Messages.locale())));
    }
    DateTimeFormatter format = at.getYear() == today.getYear() ? DAY_MONTH : FULL;
    return at.format(format.withLocale(Messages.locale()));
  }

  private static String capitalize(String s) {
    return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Messages.locale()) + s.substring(1);
  }
}
