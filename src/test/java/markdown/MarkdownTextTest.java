package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MarkdownTextTest {

  @Test
  void plainTextRoundTrips() {
    assertEquals("hello world", MarkdownText.write(MarkdownText.parse("hello world")));
  }

  @Test
  void parsesEachStyle() {
    assertTrue(((MdText) MarkdownText.parse("**b**").get(0)).bold());
    assertTrue(((MdText) MarkdownText.parse("*i*").get(0)).italic());
    assertTrue(((MdText) MarkdownText.parse("__u__").get(0)).underline());
    assertTrue(((MdText) MarkdownText.parse("~~s~~").get(0)).strike());
    assertTrue(((MdText) MarkdownText.parse("```c```").get(0)).code());
  }

  @Test
  void styleRoundTripsIncludingCombination() {
    for (String md : List.of("**b**", "*i*", "__u__", "~~s~~", "```c```", "```**x**```")) {
      assertEquals(md, MarkdownText.write(MarkdownText.parse(md)), md);
    }
  }

  @Test
  void textAfterClosingMarkerIsPlain() {
    List<MdNode> nodes = MarkdownText.parse("**b**plain");

    MdText plain = (MdText) nodes.get(1);
    assertEquals("plain", plain.text());
    assertFalse(plain.bold());
  }

  @Test
  void parsesImageWithAndWithoutSize() {
    MdImage sized = (MdImage) MarkdownText.parse("![|300x200](img/a.png)").get(0);
    assertEquals("img/a.png", sized.path());
    assertEquals(300, sized.width());
    assertEquals(200, sized.height());

    MdImage natural = (MdImage) MarkdownText.parse("![](b.png)").get(0);
    assertEquals(0, natural.width());
  }

  @Test
  void imageRoundTrips() {
    assertEquals(
        "![|300x200](a.png)", MarkdownText.write(MarkdownText.parse("![|300x200](a.png)")));
    assertEquals("![](a.png)", MarkdownText.write(MarkdownText.parse("![](a.png)")));
  }

  @Test
  void mixesTextAndImage() {
    List<MdNode> nodes = MarkdownText.parse("see ![](a.png) here");

    assertEquals(3, nodes.size());
    assertInstanceOf(MdText.class, nodes.get(0));
    assertInstanceOf(MdImage.class, nodes.get(1));
    assertInstanceOf(MdText.class, nodes.get(2));
  }

  @Test
  void malformedImageStaysText() {
    List<MdNode> nodes = MarkdownText.parse("![oops](no-close");

    assertInstanceOf(MdText.class, nodes.get(0));
  }
}
