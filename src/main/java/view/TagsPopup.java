package view;

import static view.Messages.tr;

import java.util.Map;
import javax.swing.JPopupMenu;

import util.NoteIndex;

/** The list of all {@code #tags} with their note counts; picking one filters the browser by it. */
final class TagsPopup {

  private TagsPopup() {}

  static void show(GroupTree tree, SearchField search) {
    NoteIndex index = tree.query().filter().index();
    Map<String, Integer> tags = index == null ? Map.of() : index.tagCounts();
    JPopupMenu menu = new JPopupMenu();
    tags.forEach(
        (tag, count) ->
            menu.add(
                EditorMenu.item(
                    "#" + tag + "   " + count, null, () -> search.input().setText("#" + tag))));
    if (tags.isEmpty()) {
      menu.add(EditorMenu.item(tr("browser.noResults"), null, () -> {})).setEnabled(false);
    }
    MenuTheme.stylePopup(menu, UiPalette.current());
    menu.show(search, 0, search.getHeight());
  }
}
