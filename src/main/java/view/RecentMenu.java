package view;

import static view.Messages.tr;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import model.Tab;
import model.TabMeta;

/** File › Recent notes: the most recently edited notes, listed each time the menu opens. */
final class RecentMenu {

  private static final int RECENT = 10;

  private RecentMenu() {}

  static JMenu build(AppCommands app) {
    JMenu recent = new JMenu(tr("menu.file.recent"));
    recent.addMenuListener(
        new MenuListener() {
          @Override
          public void menuSelected(MenuEvent e) {
            fill(recent, app);
          }

          @Override
          public void menuDeselected(MenuEvent e) {
            // nothing to clean up
          }

          @Override
          public void menuCanceled(MenuEvent e) {
            // nothing to clean up
          }
        });
    return recent;
  }

  /** Fills the "Recent notes" menu with the most recently edited notes. */
  static void fill(JMenu menu, AppCommands app) {
    menu.removeAll();
    List<TabMeta> recent =
        app.context().repo().listMeta().stream()
            .filter(m -> !Tab.DEFAULT_ID.equals(m.id()))
            .sorted(Comparator.comparing(TabMeta::lastModified).reversed())
            .limit(RECENT)
            .toList();
    for (TabMeta note : recent) {
      String when = RelativeTime.format(note.lastModified(), Instant.now(), ZoneId.systemDefault());
      menu.add(
          EditorMenu.item(
              note.name() + "   ·   " + when,
              null,
              () -> app.context().session().openExisting(note.id())));
    }
    if (recent.isEmpty()) {
      menu.add(EditorMenu.item(tr("menu.file.recent.empty"), null, () -> {})).setEnabled(false);
    }
    MenuTheme.stylePopup(menu.getPopupMenu(), UiPalette.current());
  }
}
