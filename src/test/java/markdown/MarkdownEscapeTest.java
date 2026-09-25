package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Literal marker characters typed by the user must survive a save/load cycle unchanged. */
class MarkdownEscapeTest {

  private static final List<String> LITERALS =
      List.of(
          "2*3*4 = 24",
          "* buy milk\n* buy eggs",
          "my__init__var",
          "~~not struck~~",
          "```not code```",
          "a*",
          "*",
          "__",
          "___",
          "``",
          "trailing\\",
          "\\*",
          "\\\\*",
          "\\__x",
          "C:\\Users\\me",
          "\\\\server\\share",
          "back\\slash\\",
          "snake_case_",
          "tilde~",
          "tick`");

  @Test
  void literalMarkersRoundTripAsPlainText() {
    for (String literal : LITERALS) {
      MdText plain = new MdText(literal, false, false, false, false, false);

      List<MdNode> parsed = MarkdownText.parse(MarkdownText.write(List.of(plain)));

      assertEquals(List.of(plain), parsed, literal);
    }
  }

  @Test
  void literalMarkersRoundTripInsideEveryStyle() {
    for (String literal : LITERALS) {
      for (MdText styled : styledVariants(literal)) {
        List<MdNode> parsed = MarkdownText.parse(MarkdownText.write(List.of(styled)));

        assertEquals(List.of(styled), parsed, literal + " / " + styled);
      }
    }
  }

  @Test
  void backslashBeforeStyledRunKeepsBothTheBackslashAndTheStyle() {
    List<MdNode> nodes =
        List.of(
            new MdText("a\\", false, false, false, false, false),
            new MdText("b", true, false, false, false, false));

    assertEquals(nodes, MarkdownText.parse(MarkdownText.write(nodes)));
  }

  @Test
  void plainWindowsPathsAreStoredVerbatim() {
    MdText path = new MdText("C:\\Users\\me", false, false, false, false, false);

    assertEquals("C:\\Users\\me", MarkdownText.write(List.of(path)));
  }

  @Test
  void legacyDoubleBackslashIsNotCollapsed() {
    assertEquals("\\\\server", ((MdText) MarkdownText.parse("\\\\server").get(0)).text());
  }

  @Test
  void loneTrailingBackslashInLegacyTextIsKept() {
    assertEquals("dir\\", ((MdText) MarkdownText.parse("dir\\").get(0)).text());
  }

  @Test
  void escapedAsteriskIsNotItalic() {
    MdText text = (MdText) MarkdownText.parse("2\\*3\\*4").get(0);

    assertEquals("2*3*4", text.text());
    assertFalse(text.italic());
  }

  private static List<MdText> styledVariants(String t) {
    return List.of(
        new MdText(t, true, false, false, false, false),
        new MdText(t, false, true, false, false, false),
        new MdText(t, false, false, true, false, false),
        new MdText(t, false, false, false, true, false),
        new MdText(t, false, false, false, false, true),
        new MdText(t, true, true, true, true, true));
  }
}
