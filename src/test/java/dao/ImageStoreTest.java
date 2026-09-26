package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageStoreTest {

  @Test
  void savedImagesResolveInsideTheImagesFolder(@TempDir Path data) {
    ImageStore store = new ImageStore(data);

    String path = store.save(new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB));

    assertTrue(path.startsWith("images/img_") && path.endsWith(".png"));
    Path file = store.resolve(path);
    assertNotNull(file);
    assertTrue(Files.isRegularFile(file));
  }

  @Test
  void pathsLeavingTheImagesFolderAreRefused(@TempDir Path data) {
    ImageStore store = new ImageStore(data);

    assertNull(store.resolve("../../etc/passwd"));
    assertNull(store.resolve("images/../cutenotes_options.txt"));
    assertNull(store.resolve(data.resolve("secret.png").toAbsolutePath().toString()));
    assertNull(store.resolve("images"));
    assertNull(store.resolve(""));
    assertNull(store.resolve(null));
    assertNull(store.resolve("images/\0bad"));
    assertNotNull(store.resolve("images/./a.png"));
  }

  @Test
  void garbageCollectionKeepsReferencedAndRecentImages(@TempDir Path data) throws Exception {
    ImageStore store = new ImageStore(data);
    String used = store.save(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    String orphan = store.save(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    String fresh = store.save(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    Instant now = Instant.now();
    FileTime old = FileTime.from(now.minus(Duration.ofDays(3)));
    Files.setLastModifiedTime(store.resolve(used), old);
    Files.setLastModifiedTime(store.resolve(orphan), old);

    int deleted =
        store.collectGarbage(Set.of(used, "../../escape.png"), now.minus(Duration.ofDays(1)));

    assertEquals(1, deleted);
    assertTrue(Files.exists(store.resolve(used)));
    assertFalse(Files.exists(store.resolve(orphan)));
    assertTrue(Files.exists(store.resolve(fresh)));
  }

  @Test
  void noImagesFolderMeansNothingToCollect(@TempDir Path data) throws Exception {
    assertEquals(0, new ImageStore(data).collectGarbage(Set.of(), Instant.now()));
  }
}
