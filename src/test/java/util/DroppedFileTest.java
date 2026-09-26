package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DroppedFileTest {

  @Test
  void textFilesBecomeNotesAndPicturesImages() {
    assertEquals(DroppedFile.NOTE, DroppedFile.of("todo.MD"));
    assertEquals(DroppedFile.NOTE, DroppedFile.of("a.txt"));
    assertEquals(DroppedFile.IMAGE, DroppedFile.of("shot.PNG"));
    assertEquals(DroppedFile.IMAGE, DroppedFile.of("p.jpeg"));
    assertEquals(DroppedFile.UNSUPPORTED, DroppedFile.of("app.exe"));
    assertEquals(DroppedFile.UNSUPPORTED, DroppedFile.of("README"));
  }

  @Test
  void theNoteIsNamedAfterTheFile() {
    assertEquals("todo", DroppedFile.title("todo.md"));
    assertEquals("a.b", DroppedFile.title("a.b.txt"));
    assertEquals(".hidden", DroppedFile.title(".hidden"));
  }
}
