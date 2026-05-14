package view;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class DocumentChanges {

  private DocumentChanges() {}

  static DocumentListener on(Runnable handler) {
    return new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        handler.run();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        handler.run();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        handler.run();
      }
    };
  }
}
