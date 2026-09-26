package util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Asks GitHub for the latest release and compares its tag with this build's. */
public final class UpdateChecker {

  public static final String RELEASES_PAGE = "https://github.com/gneginskiy/cutenotes/releases";
  static final URI LATEST =
      URI.create("https://api.github.com/repos/gneginskiy/cutenotes/releases/latest");
  private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern ID = Pattern.compile("v?(\\d{4})-(\\d{2})-(\\d{2})(?:-(\\d+))?");

  private UpdateChecker() {}

  /** The latest release tag, or {@code null} when GitHub cannot be reached. */
  public static String latestTag(HttpClient client) {
    try {
      HttpRequest request =
          HttpRequest.newBuilder(LATEST)
              .timeout(Duration.ofSeconds(8))
              .header("Accept", "application/vnd.github+json")
              .header("User-Agent", "cuteNotes/" + AppVersion.current())
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return response.statusCode() == 200 ? parseTag(response.body()) : null;
    } catch (Exception e) {
      return null;
    }
  }

  static String parseTag(String json) {
    Matcher m = TAG.matcher(json == null ? "" : json);
    return m.find() ? m.group(1) : null;
  }

  /** Whether release {@code candidate} is newer than {@code current}; dev builds never update. */
  public static boolean newer(String candidate, String current) {
    long[] a = parse(candidate);
    long[] b = parse(current);
    if (a == null || b == null) {
      return false;
    }
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) {
        return a[i] > b[i];
      }
    }
    return false;
  }

  /** Page of a release (or of all releases for unknown / dev versions). */
  public static String pageOf(String tag) {
    return parse(tag) == null ? RELEASES_PAGE : RELEASES_PAGE + "/tag/" + tag;
  }

  private static long[] parse(String id) {
    Matcher m = ID.matcher(id == null ? "" : id.trim());
    if (!m.matches()) {
      return null;
    }
    long n = m.group(4) == null ? 0 : Long.parseLong(m.group(4));
    return new long[] {
      Long.parseLong(m.group(1)), Long.parseLong(m.group(2)), Long.parseLong(m.group(3)), n
    };
  }
}
