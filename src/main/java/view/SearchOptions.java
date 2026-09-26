package view;

/** How the find bar matches: case, whole words only, or the query as a regular expression. */
record SearchOptions(boolean caseSensitive, boolean wholeWord, boolean regex) {

  static final SearchOptions PLAIN = new SearchOptions(false, false, false);
}
