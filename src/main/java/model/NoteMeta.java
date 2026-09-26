package model;

/**
 * Per-note settings that are not part of the note's text.
 *
 * @param pinned shown on top in the notes browser
 * @param color tab colour name ({@code red}, {@code green}…); empty for none
 * @param autoTitle the note's name follows its first line until the user renames it
 */
public record NoteMeta(String id, boolean pinned, String color, boolean autoTitle) {

  public static NoteMeta defaults(String id) {
    return new NoteMeta(id, false, "", false);
  }

  public boolean isDefault() {
    return !pinned && color.isEmpty() && !autoTitle;
  }

  public NoteMeta withPinned(boolean value) {
    return new NoteMeta(id, value, color, autoTitle);
  }

  public NoteMeta withColor(String value) {
    return new NoteMeta(id, pinned, value == null ? "" : value, autoTitle);
  }

  public NoteMeta withAutoTitle(boolean value) {
    return new NoteMeta(id, pinned, color, value);
  }
}
