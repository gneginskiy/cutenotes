package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExportFormatTest {

  @Test
  void rendersEachFormat() {
    String md = "# Title\n**bold** ![](images/a.png)";

    assertEquals(md, ExportFormat.MARKDOWN.render("T", md, p -> p));
    assertEquals("# Title\nbold ", ExportFormat.TEXT.render("T", md, p -> p));
    assertTrue(ExportFormat.HTML.render("T", md, p -> "x/" + p).contains("src=\"x/images/a.png\""));
  }

  @Test
  void picksTheFormatByExtensionAndAddsItWhenMissing() {
    assertEquals(ExportFormat.HTML, ExportFormat.ofFile("a.HTML", ExportFormat.TEXT));
    assertEquals(ExportFormat.TEXT, ExportFormat.ofFile("a.doc", ExportFormat.TEXT));
    assertEquals("a.md", ExportFormat.MARKDOWN.withExtension("a.md"));
    assertEquals("a.txt.md", ExportFormat.MARKDOWN.withExtension("a.txt"));
    assertEquals("md", ExportFormat.MARKDOWN.extension());
  }

  @Test
  void namesAreSafeForEveryFileSystem() {
    assertEquals("a_b_ c", ExportFormat.safeName("a/b: c"));
    assertEquals("note", ExportFormat.safeName("  "));
    assertEquals("note.hidden", ExportFormat.safeName(".hidden"));
    assertEquals(80, ExportFormat.safeName("x".repeat(200)).length());
  }
}
