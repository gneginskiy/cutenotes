package model;

public record Tab(String id, String name, String content) {

  /** Id of the scratch area shown when no tab is open; it is never part of the session. */
  public static final String DEFAULT_ID = "__default__";

  /** Whether the tab belongs to the list of tabs restored on the next start. */
  public boolean inSession() {
    return !DEFAULT_ID.equals(id) && !content.isBlank();
  }
}
