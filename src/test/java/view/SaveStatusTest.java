package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class SaveStatusTest {

  private final AtomicReference<String> title = new AtomicReference<>();
  private final List<Exception> alerts = new ArrayList<>();
  private final SaveStatus status = new SaveStatus(title::set, alerts::add, "Notes");

  @Test
  void failureMarksTheTitleAndAlertsOncePerEpisode() {
    Exception diskFull = new IllegalStateException("No space left on device");

    status.update(diskFull);
    status.update(diskFull);

    assertEquals(SaveStatus.WARNING + "Notes", title.get());
    assertEquals(List.of(diskFull), alerts);
  }

  @Test
  void recoveryRestoresTheTitleAndANewEpisodeAlertsAgain() {
    status.update(new IllegalStateException("x"));
    status.update(null);
    assertEquals("Notes", title.get());

    status.update(new IllegalStateException("y"));
    assertEquals(2, alerts.size());
  }

  @Test
  void renamingTheWindowKeepsTheWarning() {
    status.update(new IllegalStateException("x"));

    status.setTitle("My notes");

    assertEquals(SaveStatus.WARNING + "My notes", title.get());
  }
}
