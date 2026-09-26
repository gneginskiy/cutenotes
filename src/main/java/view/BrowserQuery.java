package view;

import static view.Messages.tr;

import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;

import model.GroupData;
import model.TabMeta;

/** What the notes browser shows ({@link NoteFilter}) and in which order; pinned notes lead. */
final class BrowserQuery {

  /** How notes are ordered inside a group. */
  enum Sort {
    MANUAL("browser.sort.manual"),
    RECENT("browser.sort.recent"),
    NAME("browser.sort.name");

    private final String key;

    Sort(String key) {
      this.key = key;
    }

    @Override
    public String toString() {
      return tr(key);
    }
  }

  private final NoteMetas metas;
  private NoteFilter filter;
  private Sort sort = Sort.MANUAL;

  BrowserQuery(Set<String> openIds, NoteMetas metas) {
    this.filter = NoteFilter.all(openIds);
    this.metas = metas;
  }

  NoteFilter filter() {
    return filter;
  }

  void change(UnaryOperator<NoteFilter> update) {
    filter = update.apply(filter);
  }

  NoteMetas metas() {
    return metas;
  }

  Sort sort() {
    return sort;
  }

  void setSort(Sort value) {
    sort = value;
  }

  /** Dragging reorders by hand: only in manual order with nothing filtered away. */
  boolean reorderable() {
    return !filter.searching() && sort == Sort.MANUAL;
  }

  Comparator<TabMeta> order(GroupData data) {
    Comparator<TabMeta> pinnedFirst = Comparator.comparing(m -> !metas.get(m.id()).pinned());
    Comparator<TabMeta> mode =
        switch (sort) {
          case MANUAL -> Comparator.comparingInt(m -> data.orderIndex(m.id()));
          case RECENT -> Comparator.comparing(TabMeta::lastModified).reversed();
          case NAME -> Comparator.comparing(m -> m.name().toLowerCase(Locale.ROOT));
        };
    return pinnedFirst.thenComparing(mode);
  }
}
