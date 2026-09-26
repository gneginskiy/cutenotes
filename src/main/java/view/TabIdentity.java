package view;

import dao.NoteMetaStore;

/**
 * What a tab is called and how it is marked: automatic titles from the first line, manual renames
 * and the note colour, backed by the per-note settings ({@link NoteMetas}).
 */
final class TabIdentity {

  private final OpenTabs tabs;
  private final TabHeader header;
  private NoteMetas metas = NoteMetas.inMemory();
  private AutoTitles titles = new AutoTitles(metas, this::applyTitle);

  TabIdentity(OpenTabs tabs, TabHeader header) {
    this.tabs = tabs;
    this.header = header;
  }

  /** Switches to the stored per-note settings; call before any tab is opened. */
  void useMetas(NoteMetaStore store) {
    this.metas = new NoteMetas(store);
    this.titles = new AutoTitles(metas, this::applyTitle);
  }

  NoteMetas metas() {
    return metas;
  }

  void opened(String id, String name, NoteEditor area) {
    titles.attach(id, name, area);
    refreshColor(id);
  }

  /** Applies a pending automatic title before the tab's final state is taken. */
  void closing(String id) {
    TabState open = tabs.get(id);
    if (open != null) {
      titles.retitle(id, open.area);
      titles.detach(id);
    }
  }

  void renamedByUser(String id, String name) {
    tabs.rename(id, name);
    titles.manual(id);
  }

  /** Shows the colour the note was marked with on its tab. */
  void refreshColor(String id) {
    header.setColor(id, metas.get(id).color());
  }

  void setColor(String id, String color) {
    metas.put(metas.get(id).withColor(color));
    refreshColor(id);
  }

  private void applyTitle(String id, String name) {
    TabState s = tabs.get(id);
    if (s != null && !name.equals(s.name)) {
      tabs.rename(id, name);
      header.setTitle(id, name);
    }
  }
}
