package view;

import static view.Messages.tr;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import javax.swing.JFrame;

import dao.NoteHistory;

/**
 * Earlier versions of the current note. Restoring one first records the current text as a version
 * and replaces the text as a single undo step, so nothing is ever lost by restoring.
 */
final class HistoryDialog {

  private HistoryDialog() {}

  static ListPreviewDialog<Instant> open(
      JFrame owner, NoteHistory history, String id, String name, NoteEditor editor) {
    ListPreviewDialog<Instant> dialog =
        new ListPreviewDialog<>(
            owner,
            tr("history.title", name),
            tr("history.empty"),
            version -> history.load(id, version));
    dialog.addAction(
        tr("history.restore"),
        version -> {
          restore(history, id, editor, history.load(id, version));
          dialog.dispose();
        });
    dialog.addButton(tr("history.close"), dialog::dispose);
    Instant now = Instant.now();
    List<ListPreviewDialog.Entry<Instant>> rows =
        history.versions(id).stream()
            .map(
                v ->
                    new ListPreviewDialog.Entry<>(
                        v, RelativeTime.format(v, now, ZoneId.systemDefault())))
            .toList();
    dialog.setEntries(rows);
    dialog.setVisible(true);
    return dialog;
  }

  static void restore(NoteHistory history, String id, NoteEditor editor, String text) {
    history.snapshot(id, editor.markdown(), Instant.now(), true);
    editor.replaceText(text);
  }
}
