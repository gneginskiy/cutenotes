package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class LinksTest {

  @Test
  void findsLinksWithoutTrailingPunctuation() {
    String text = "See https://example.com/a?b=1. And (www.site.org/x) or http://x.io!";

    List<String> found = Links.find(text).stream().map(r -> text.substring(r[0], r[1])).toList();

    assertEquals(List.of("https://example.com/a?b=1", "www.site.org/x", "http://x.io"), found);
  }

  @Test
  void keepsBalancedBracketsInsideTheLink() {
    String text = "wiki https://en.wikipedia.org/wiki/Java_(language) end";

    int[] link = Links.find(text).get(0);

    assertEquals("https://en.wikipedia.org/wiki/Java_(language)", text.substring(link[0], link[1]));
  }

  @Test
  void linkAtAnOffsetIsNormalised() {
    String text = "go www.example.com now";

    assertEquals("https://www.example.com", Links.at(text, 5));
    assertNull(Links.at(text, 1));
    assertEquals("http://a.b", Links.normalize("http://a.b"));
  }

  @Test
  void emailsAndPathsAreNotLinks() {
    assertEquals(0, Links.find("mail me@www.example.com or /home/www.x").size());
  }
}
