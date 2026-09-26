package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HtmlExportTest {

  private static String line(String md) {
    List<List<MdNode>> lines = MdLines.split(md);
    return HtmlExport.line(lines.get(0), p -> "file:///data/" + p);
  }

  @Test
  void headingsListsAndCheckboxes() {
    assertEquals("<h1>Title</h1>", line("# Title"));
    assertEquals("<h3>Small</h3>", line("### Small"));
    assertEquals("<p style=\"margin-left:1em\">• <span>milk</span></p>", line("- milk"));
    assertEquals("<p style=\"margin-left:2em\">2. <span>two</span></p>", line("  2. two"));
    assertEquals("<p style=\"margin-left:1em\">☐ <span>todo</span></p>", line("- [ ] todo"));
    assertEquals(
        "<p style=\"margin-left:1em\">☑ <span class=\"done\">done</span></p>", line("- [x] done"));
    assertEquals("<p></p>", line(""));
  }

  @Test
  void inlineStylesLinksAndEscaping() {
    assertEquals("<p><strong>bold</strong> &amp; <em>it</em></p>", line("**bold** & *it*"));
    assertEquals("<p><code>a&lt;b</code></p>", line("```a<b```"));
    assertEquals("<p>see <a href=\"https://www.x.org\">www.x.org</a>.</p>", line("see www.x.org."));
    assertEquals("<p><s>gone</s> <u>under</u></p>", line("~~gone~~ __under__"));
  }

  @Test
  void imagesPointAtTheirFiles() {
    assertEquals(
        "<p>a <img src=\"file:///data/images/x.png\" alt=\"\"></p>", line("a ![](images/x.png)"));
    assertTrue(line("![|120x80](images/x.png)").contains("width=\"120\""));
  }

  @Test
  void aWholePageHasATitleAndEveryLine() {
    String page = HtmlExport.page("A <note>", "# A\n\ntext", p -> p);

    assertTrue(page.startsWith("<!DOCTYPE html>"));
    assertTrue(page.contains("<title>A &lt;note&gt;</title>"));
    assertTrue(page.contains("<h1>A</h1>\n<p></p>\n<p>text</p>\n"));
  }

  @Test
  void prefixesAreCutAcrossStyledRuns() {
    List<MdNode> line = MdLines.split("- **bold** rest").get(0);

    assertEquals("bold rest", MdLines.text(MdLines.dropPrefix(line, 2)));
    assertEquals(2, MdLines.split("a\nb").size());
  }
}
