package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LineKindTest {

  @Test
  void headingsNeedASpaceAfterTheHashes() {
    assertEquals(LineKind.H1, LineKind.of("# Title"));
    assertEquals(LineKind.H2, LineKind.of("## Part"));
    assertEquals(LineKind.H3, LineKind.of("### Detail"));
    assertEquals(LineKind.PLAIN, LineKind.of("#tag"));
    assertEquals(LineKind.PLAIN, LineKind.of("#### too deep"));
    assertTrue(LineKind.H2.heading());
    assertTrue(LineKind.H1.scale() > LineKind.H2.scale());
    assertTrue(LineKind.H2.scale() > LineKind.H3.scale());
  }

  @Test
  void checkedTasksAreDone() {
    assertEquals(LineKind.DONE, LineKind.of("  - [x] shipped"));
    assertEquals(LineKind.DONE, LineKind.of("- [x] with line break\nnext"));
    assertEquals(LineKind.PLAIN, LineKind.of("- [ ] open"));
    assertEquals(LineKind.PLAIN, LineKind.of("text"));
    assertFalse(LineKind.DONE.heading());
    assertEquals(1f, LineKind.DONE.scale());
  }
}
