package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileDropTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @Test
  void textFilesOpenAsNotesAndOtherFilesAreExplained(@TempDir Path dir) throws Exception {
    File note = Files.writeString(dir.resolve("todo.md"), "- milk").toFile();
    File binary = Files.writeString(dir.resolve("app.exe"), "x").toFile();
    File broken = Files.writeString(dir.resolve("broken.png"), "not a png").toFile();
    List<File> opened = new ArrayList<>();
    List<String> messages = new ArrayList<>();
    FileDrop.use(opened::add, messages::add);

    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor pane = new NoteEditor("text");
          TransferHandler handler = pane.getTransferHandler();
          TransferHandler.TransferSupport drop =
              new TransferHandler.TransferSupport(pane, files(List.of(note, binary, broken)));

          assertTrue(handler.canImport(drop));
          assertTrue(handler.importData(drop));
          assertEquals("text", pane.markdown(), "an unreadable picture inserts nothing");
        });

    assertEquals(List.of(note), opened);
    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("app.exe"));
  }

  @Test
  void droppedPicturesAreInsertedAndTextStillPastes(@TempDir Path dir) throws Exception {
    File picture = dir.resolve("p.png").toFile();
    ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", picture);

    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor pane = new NoteEditor("");
          TransferHandler handler = pane.getTransferHandler();
          handler.importData(new TransferHandler.TransferSupport(pane, files(List.of(picture))));
          assertTrue(pane.markdown().contains("!["), pane.markdown());

          NoteEditor plain = new NoteEditor("");
          plain
              .getTransferHandler()
              .importData(new TransferHandler.TransferSupport(plain, new StringSelection("hi")));
          assertEquals("hi", plain.markdown());
        });
  }

  private static Transferable files(List<File> files) {
    return new Transferable() {
      @Override
      public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {DataFlavor.javaFileListFlavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.javaFileListFlavor.equals(flavor);
      }

      @Override
      public Object getTransferData(DataFlavor flavor) {
        return files;
      }
    };
  }
}
