package util;

/**
 * What to do when a note's file changed outside the app (another editor, a sync service): take the
 * disk version when there are no unsaved edits, keep both when there are.
 */
public enum DiskChange {
  /** Nothing new on disk (or it already matches the editor). */
  NONE,
  /** The editor has no unsaved edits: show the disk version. */
  RELOAD,
  /** Both changed: keep the editor's text and save the disk version as a copy. */
  CONFLICT;

  /**
   * {@code known} is what the app last wrote or read, {@code onDisk} the file now and {@code
   * inEditor} the text in the window.
   */
  public static DiskChange decide(String known, String onDisk, String inEditor) {
    if (onDisk.equals(known) || onDisk.equals(inEditor)) {
      return NONE;
    }
    return inEditor.equals(known) ? RELOAD : CONFLICT;
  }
}
