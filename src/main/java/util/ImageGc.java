package util;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dao.ImageStore;
import markdown.MarkdownText;
import markdown.MdImage;
import markdown.MdNode;

/**
 * Removes pasted images that no note refers to any more. Before, deleting an image from a note (or
 * the note itself) left its file behind, and the images folder only ever grew.
 */
public final class ImageGc {

  /** Images younger than this survive: they may have been pasted and not saved yet. */
  static final Duration GRACE = Duration.ofDays(1);

  private ImageGc() {}

  /** The image paths referenced by the given note texts (notes, trash and history). */
  public static Set<String> references(Collection<String> texts) {
    Set<String> paths = new HashSet<>();
    for (String text : texts) {
      for (MdNode node : MarkdownText.parse(text)) {
        if (node instanceof MdImage image) {
          paths.add(image.path());
        }
      }
    }
    return paths;
  }

  /**
   * Deletes unreferenced images older than a day; returns how many. Does nothing while a
   * password-protected note exists: the images it references cannot be seen.
   */
  public static int run(ImageStore store, Collection<String> texts, Instant now)
      throws IOException {
    for (String text : texts) {
      if (NoteCrypto.isEncrypted(text)) {
        return 0;
      }
    }
    return store.collectGarbage(references(texts), now.minus(GRACE));
  }
}
