package view;

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
      return "just now";
    }
    if (age.toMinutes() < 60) {
      return age.toMinutes() + " min ago";
    }
    ZonedDateTime at = then.atZone(zone);
    LocalDate today = now.atZone(zone).toLocalDate();
    long days = ChronoUnit.DAYS.between(at.toLocalDate(), today);
    if (days == 0) {
      return at.format(TIME);
    }
    if (days == 1) {
      return "Yesterday";
    }
    if (days < 7) {
      return at.format(WEEKDAY);
    }
    return at.getYear() == today.getYear() ? at.format(DAY_MONTH) : at.format(FULL);
  }
}
