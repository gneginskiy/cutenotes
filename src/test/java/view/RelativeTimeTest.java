package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

class RelativeTimeTest {

  private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
  private static final Instant NOW = ZonedDateTime.of(2026, 9, 26, 15, 30, 0, 0, ZONE).toInstant();

  @Test
  void recentChangesAreRelative() {
    assertEquals("just now", format(Duration.ofSeconds(20)));
    assertEquals("just now", RelativeTime.format(NOW.plusSeconds(90), NOW, ZONE), "clock skew");
    assertEquals("1 min ago", format(Duration.ofSeconds(61)));
    assertEquals("59 min ago", format(Duration.ofMinutes(59)));
  }

  @Test
  void sameDayShowsTheTime() {
    assertEquals("09:05", format(Duration.ofHours(6).plusMinutes(25)));
  }

  @Test
  void olderChangesShowDayNamesThenDates() {
    assertEquals("Yesterday", format(Duration.ofHours(20)));
    assertEquals("Tuesday", format(Duration.ofDays(4)));
    assertEquals("12 Sep", format(Duration.ofDays(14)));
    assertEquals("26 Sep 2025", format(Duration.ofDays(365)));
  }

  private static String format(Duration age) {
    return RelativeTime.format(NOW.minus(age), NOW, ZONE);
  }
}
