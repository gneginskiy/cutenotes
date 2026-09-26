package view;

import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;
import model.Theme;

/** The note editor: a styled {@link JTextPane} whose content round-trips through Markdown. */
class NoteEditor extends JTextPane {

  private static final int PAD_X = 18;
  private static final int PAD_Y = 12;

  private final transient EditorUndo undo = new EditorUndo(this);
  private final transient MarkdownCache markdownCache = new MarkdownCache(getStyledDocument());
  private int selectedImage = -1;

  NoteEditor(String markdown) {
    setBorder(BorderFactory.createEmptyBorder(PAD_Y, PAD_X, PAD_Y, PAD_X));
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
    EditorTheme.apply(this, ThemeHolder.current());
    LineOps.install(this, undo);
    EditorLists.install(this);
    EditorFormat.install(this);
    EditorImages.install(this);
    EditorDecorations.install(this);
    EditorMenu.install(this);
    addCaretListener(e -> clearImageInputAttributes());
    setMarkdown(markdown);
    CaretHistory.install(this);
    FileDrop.install(this);
  }

  /** Called by the JTextPane constructor, before the document and its views exist. */
  @Override
  protected EditorKit createDefaultEditorKit() {
    return new NoteEditorKit();
  }

  /**
   * The styled editor kit copies the character attributes under the caret into the typing
   * attributes. Next to an inline image that would make freshly typed text inherit the image's
   * icon/path — producing phantom selection frames and duplicate {@code ![](...)} on save. Strip
   * them so typing is always plain.
   */
  private void clearImageInputAttributes() {
    MutableAttributeSet in = getInputAttributes();
    in.removeAttribute(StyleConstants.IconAttribute);
    in.removeAttribute(ImageAttr.PATH);
    in.removeAttribute(ImageAttr.SOURCE);
    in.removeAttribute(ImageAttr.WIDTH);
    in.removeAttribute(ImageAttr.HEIGHT);
  }

  void applyTheme(Theme t) {
    EditorTheme.apply(this, t);
    CodeRestyle.apply(getStyledDocument(), t);
    EditorDecorations.refresh(this);
    TabPanes.refit(this);
  }

  /**
   * Keeps lines at most {@code lineWidth} characters long by centring the text in a wide window (0
   * means the full width); the side padding never drops below the normal one.
   */
  void fitWidth(int viewportWidth, int lineWidth) {
    int pad = PAD_X;
    if (lineWidth > 0 && viewportWidth > 0) {
      int column = lineWidth * getFontMetrics(getFont()).charWidth('n');
      pad = Math.max(PAD_X, (viewportWidth - column) / 2);
    }
    if (getInsets().left != pad) {
      setBorder(BorderFactory.createEmptyBorder(PAD_Y, pad, PAD_Y, pad));
      revalidate();
    }
  }

  EditorUndo undoHistory() {
    return undo;
  }

  /** A document change applied as a single undo step. */
  interface DocEdit {
    void run(StyledDocument doc) throws Exception;
  }

  @SneakyThrows
  void editAsOneStep(DocEdit change) {
    undo.beginGroup();
    try {
      change.run(getStyledDocument());
    } finally {
      undo.endGroup();
    }
  }

  void setSelectedImage(int offset) {
    selectedImage = offset;
    repaint();
  }

  int selectedImage() {
    return selectedImage;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    EmptyHint.paint(g, this);
    StyledDocument doc = getStyledDocument();
    if (selectedImage < 0
        || selectedImage >= doc.getLength()
        || !ImageAttr.isImage(doc.getCharacterElement(selectedImage).getAttributes())) {
      selectedImage = -1;
      return;
    }
    ImageHandle.paint(g, this, selectedImage);
  }

  String markdown() {
    return markdownCache.get();
  }

  /** Replaces the whole text as one undo step (restoring a version, reloading from disk). */
  void replaceText(String markdown) {
    editAsOneStep(doc -> DocumentMarkdown.applyMarkdown(doc, markdown));
    setCaretPosition(0);
    EditorDecorations.refresh(this);
  }

  final void setMarkdown(String markdown) {
    DocumentMarkdown.applyMarkdown(getStyledDocument(), markdown);
    setCaretPosition(0);
    undo.discardAll();
    EditorDecorations.refresh(this);
  }
}
