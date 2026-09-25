package view;

import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;

/**
 * Memoises a document's Markdown until the next change. The auto-saver snapshots every open tab on
 * the EDT every 300 ms; re-serialising unchanged documents made typing lag with large notes.
 */
final class MarkdownCache {

  private final StyledDocument doc;
  private long version;
  private long cachedVersion = -1;
  private String cached;

  MarkdownCache(StyledDocument doc) {
    this.doc = doc;
    doc.addDocumentListener(DocumentChanges.on(() -> version++));
  }

  String get() {
    if (cachedVersion == version) {
      return cached;
    }
    String markdown = DocumentMarkdown.toMarkdown(doc);
    if (SwingUtilities.isEventDispatchThread()) {
      // an off-EDT read (exit fallback) may see a half-applied edit: never memoise that
      cached = markdown;
      cachedVersion = version;
    }
    return markdown;
  }
}
