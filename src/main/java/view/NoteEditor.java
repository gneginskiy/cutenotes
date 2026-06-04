package view;

import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import model.Theme;

/** The note editor: a styled {@link JTextPane} whose content round-trips through Markdown. */
class NoteEditor extends JTextPane {

  private final transient UndoManager undo = new UndoManager();
  private int selectedImage = -1;

  NoteEditor(String markdown) {
    setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
    EditorTheme.apply(this, ThemeHolder.current());
    getDocument().addUndoableEditListener(e -> undo.addEdit(e.getEdit()));
    installUndoRedo();
    LineOps.install(this);
    EditorFormat.install(this);
    EditorImages.install(this);
    setMarkdown(markdown);
  }

  void applyTheme(Theme t) {
    EditorTheme.apply(this, t);
    SimpleAttributeSet on = new SimpleAttributeSet();
    EditorFormat.styleCode(on, true, t);
    SimpleAttributeSet off = new SimpleAttributeSet();
    EditorFormat.styleCode(off, false, t);
    StyledDocument doc = getStyledDocument();
    for (int i = 0; i < doc.getLength(); i++) {
      AttributeSet a = doc.getCharacterElement(i).getAttributes();
      if (a.getAttribute(EditorFormat.CODE) == Boolean.TRUE) {
        doc.setCharacterAttributes(i, 1, on, false);
      } else if (a.isDefined(StyleConstants.Foreground) || a.isDefined(StyleConstants.Background)) {
        doc.setCharacterAttributes(i, 1, off, false);
      }
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
    return DocumentMarkdown.toMarkdown(getStyledDocument());
  }

  final void setMarkdown(String markdown) {
    DocumentMarkdown.applyMarkdown(getStyledDocument(), markdown);
    setCaretPosition(0);
    undo.discardAllEdits();
  }

  private void installUndoRedo() {
    int mask = ShortcutMask.menu();
    bind(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask), this::undo);
    bind(KeyStroke.getKeyStroke(KeyEvent.VK_Y, mask), this::redo);
    bind(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask | InputEvent.SHIFT_DOWN_MASK), this::redo);
  }

  private void undo() {
    if (undo.canUndo()) {
      undo.undo();
    }
  }

  private void redo() {
    if (undo.canRedo()) {
      undo.redo();
    }
  }

  private void bind(KeyStroke ks, Runnable action) {
    Object name = ks.toString();
    getInputMap().put(ks, name);
    getActionMap()
        .put(
            name,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                action.run();
              }
            });
  }
}
