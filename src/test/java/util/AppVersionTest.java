package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppVersionTest {

  @Test
  void unfilledOrMissingVersionsAreDev() {
    assertEquals(AppVersion.DEV, AppVersion.clean(null));
    assertEquals(AppVersion.DEV, AppVersion.clean(" "));
    assertEquals(AppVersion.DEV, AppVersion.clean("${cutenotes.release}"));
    assertEquals("v2026-09-26-3", AppVersion.clean(" v2026-09-26-3 "));
    assertEquals(AppVersion.DEV, AppVersion.read("/no/such/resource"));
  }

  @Test
  void theBuildHasAVersion() {
    assertTrue(!AppVersion.current().isBlank());
    assertEquals(AppVersion.DEV.equals(AppVersion.current()), AppVersion.isDev());
  }
}
