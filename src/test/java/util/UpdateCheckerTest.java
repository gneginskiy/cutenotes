package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpClient;

import org.junit.jupiter.api.Test;

class UpdateCheckerTest {

  @Test
  void readsTheTagFromGitHubsAnswer() {
    assertEquals(
        "v2026-10-01", UpdateChecker.parseTag("{\"url\":\"x\",\"tag_name\" : \"v2026-10-01\"}"));
    assertNull(UpdateChecker.parseTag("{}"));
    assertNull(UpdateChecker.parseTag(null));
  }

  @Test
  void comparesReleaseIdsByDateThenNumber() {
    assertTrue(UpdateChecker.newer("v2026-10-01", "v2026-09-26-3"));
    assertTrue(UpdateChecker.newer("v2026-09-26-4", "v2026-09-26-3"));
    assertTrue(UpdateChecker.newer("v2026-09-26-1", "v2026-09-26"));
    assertFalse(UpdateChecker.newer("v2026-09-26-3", "v2026-09-26-3"));
    assertFalse(UpdateChecker.newer("v2026-09-25", "v2026-09-26"));
    assertFalse(UpdateChecker.newer("v2026-10-01", "dev"), "dev builds are never nagged");
    assertFalse(UpdateChecker.newer(null, "v2026-09-26"));
  }

  @Test
  void releasePages() {
    assertEquals(
        UpdateChecker.RELEASES_PAGE + "/tag/v2026-09-26", UpdateChecker.pageOf("v2026-09-26"));
    assertEquals(UpdateChecker.RELEASES_PAGE, UpdateChecker.pageOf("dev"));
  }

  @Test
  void unreachableServerMeansNoAnswer() {
    HttpClient offline =
        HttpClient.newBuilder()
            .proxy(java.net.ProxySelector.of(new java.net.InetSocketAddress("127.0.0.1", 9)))
            .build();
    assertNull(UpdateChecker.latestTag(offline));
  }
}
