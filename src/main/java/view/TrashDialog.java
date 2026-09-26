package view;

import static view.Messages.tr;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dao.TrashBin;
import model.TabMeta;
import util.NoteCrypto;

/** "Recently deleted": deleted notes stay here for 30 days; restore them or delete for good. */
final class TrashDialog {

  private TrashDialog() {}

  /** {@code restored} opens a note that was moved back. */
  static ListPreviewDialog<String> open(JFrame owner, TrashBin bin, Consumer<String> restored) {
    ListPreviewDialog<String> dialog =
        new ListPreviewDialog<>(
            owner, tr("trash.title"), tr("trash.empty"), id -> preview(bin.load(id).content()));
    dialog.addAction(
        tr("trash.restore"),
        id -> {
          bin.restore(id);
          restored.accept(id);
          fill(dialog, bin);
        });
    dialog.addAction(
        tr("trash.deleteForever"),
        id -> purgeAfterAsking(dialog, bin, "trash.deleteForever.confirm", () -> bin.purge(id)));
    dialog.addButton(
        tr("trash.emptyBin"),
        () ->
            purgeAfterAsking(
                dialog, bin, "trash.emptyBin.confirm", () -> bin.purgeOlderThan(Instant.MAX)));
    fill(dialog, bin);
    dialog.setVisible(true);
    return dialog;
  }

  static void fill(ListPreviewDialog<String> dialog, TrashBin bin) {
    Instant now = Instant.now();
    List<ListPreviewDialog.Entry<String>> rows =
        bin.list().stream()
            .sorted(Comparator.comparing(TabMeta::lastModified).reversed())
            .map(m -> new ListPreviewDialog.Entry<>(m.id(), label(m, now)))
            .toList();
    dialog.setEntries(rows);
  }

  /** A protected note shows as protected, not as its encrypted text. */
  static String preview(String content) {
    return NoteCrypto.isEncrypted(content) ? tr("lock.preview") : content;
  }

  private static String label(TabMeta note, Instant now) {
    return note.name()
        + "   ·   "
        + RelativeTime.format(note.lastModified(), now, ZoneId.systemDefault());
  }

  /** Deleting for good cannot be undone, so it is the one place that asks first. */
  private static void purgeAfterAsking(
      ListPreviewDialog<String> dialog, TrashBin bin, String questionKey, Runnable purge) {
    if (dialog.entries().isEmpty()) {
      return;
    }
    int answer =
        JOptionPane.showConfirmDialog(
            dialog, tr(questionKey), tr("trash.title"), JOptionPane.YES_NO_OPTION);
    if (answer == JOptionPane.YES_OPTION) {
      purge.run();
      fill(dialog, bin);
    }
  }
}
