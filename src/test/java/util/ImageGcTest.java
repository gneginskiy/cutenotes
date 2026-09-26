package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dao.ImageStore;

class ImageGcTest {

  @Test
  void findsImageReferencesInNoteTexts() {
    Set<String> refs =
        ImageGc.references(
            List.of("a ![|40x20](images/x.png) b", "**bold** ![](images/y.png)", "no images"));

    assertEquals(Set.of("images/x.png", "images/y.png"), refs);
  }

  @Test
  void deletesOnlyOldUnreferencedImages(@TempDir Path data) throws Exception {
    ImageStore store = new ImageStore(data);
    String kept = aged(store, 3);
    String orphan = aged(store, 3);

    int deleted = ImageGc.run(store, List.of("see ![](" + kept + ")"), Instant.now());

    assertEquals(1, deleted);
    assertTrue(Files.exists(store.resolve(kept)));
  }

  @Test
  void passwordProtectedNotesSuspendCollection(@TempDir Path data) throws Exception {
    ImageStore store = new ImageStore(data);
    String orphan = aged(store, 3);
    String locked = NoteCrypto.encrypt("![](" + orphan + ")", NoteCrypto.newKey("p".toCharArray()));

    assertEquals(0, ImageGc.run(store, List.of(locked), Instant.now()));
    assertTrue(Files.exists(store.resolve(orphan)));
  }

  private static String aged(ImageStore store, int days) throws Exception {
    String path = store.save(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    Files.setLastModifiedTime(
        store.resolve(path), FileTime.from(Instant.now().minus(Duration.ofDays(days))));
    return path;
  }
}
