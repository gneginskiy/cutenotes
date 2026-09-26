package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TagsTest {

  @Test
  void findsTagsInAnyScriptLowerCased() {
    assertEquals(
        Set.of("work", "идеи", "q3_plan", "to-do"),
        Tags.of("#Work notes #идеи and #q3_plan, #to-do #work"));
  }

  @Test
  void headingsLanguagesAnchorsAndNumbersAreNotTags() {
    assertEquals(
        Set.of(), Tags.of("# Heading\n## Part\nC# and F#, site.com/#anchor, issue #42, a&#39;"));
  }

  @Test
  void rangesIncludeTheHash() {
    String text = "x #tag y";

    int[] range = Tags.find(text).get(0);

    assertEquals("#tag", text.substring(range[0], range[1]));
    assertEquals(List.of(), Tags.find("none"));
  }
}
