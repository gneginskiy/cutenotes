package view;

/**
 * IntelliJ-style "move line up / down" as a plan over plain text. The block is every line the
 * selection touches (a selection ending at the very start of a line leaves that line out). Instead
 * of cutting and re-inserting the block, the neighbouring line is copied to the other side of the
 * block and the original is removed: the document never gets shorter mid-way, so the view does not
 * scroll, and the block's own text (and styling) is never touched.
 *
 * @param insertAt where the neighbour's copy goes
 * @param copyFrom start of the neighbour's text to copy
 * @param copyTo end of the neighbour's text to copy
 * @param newlineBefore insert a newline before the copy (the block was the newline-less last line)
 * @param newlineAfter insert a newline after the copy (the neighbour was the newline-less last
 *     line)
 * @param removeFrom start of the text to remove, in the document after the insertion
 * @param removeTo end of the text to remove, in the document after the insertion
 * @param shift how far the block (and so the selection) moved
 */
record LineMove(
    int insertAt,
    int copyFrom,
    int copyTo,
    boolean newlineBefore,
    boolean newlineAfter,
    int removeFrom,
    int removeTo,
    int shift) {

  /** The plan for moving the selected lines one line up (-1) or down (+1); null at an edge. */
  static LineMove plan(String text, int selStart, int selEnd, int direction) {
    int blockStart = Lines.start(text, selStart);
    int lastLineAt = selEnd > selStart && Lines.start(text, selEnd) == selEnd ? selEnd - 1 : selEnd;
    int blockEnd = Lines.endInclusive(text, Math.max(blockStart, lastLineAt));
    return direction < 0 ? up(text, blockStart, blockEnd) : down(text, blockStart, blockEnd);
  }

  private static LineMove up(String text, int blockStart, int blockEnd) {
    if (blockStart == 0) {
      return null;
    }
    int above = Lines.start(text, blockStart - 1);
    boolean blockIsLast = !text.substring(blockStart, blockEnd).endsWith("\n");
    int copyTo = blockIsLast ? blockStart - 1 : blockStart;
    return new LineMove(
        blockEnd, above, copyTo, blockIsLast, false, above, blockStart, above - blockStart);
  }

  private static LineMove down(String text, int blockStart, int blockEnd) {
    if (blockEnd == text.length() && !text.substring(blockStart, blockEnd).endsWith("\n")) {
      return null;
    }
    int belowEnd = Lines.endInclusive(text, blockEnd);
    boolean belowIsLast = !text.substring(blockEnd, belowEnd).endsWith("\n");
    int inserted = (belowEnd - blockEnd) + (belowIsLast ? 1 : 0);
    int removeFrom = blockEnd + inserted - (belowIsLast ? 1 : 0);
    return new LineMove(
        blockStart,
        blockEnd,
        belowEnd,
        false,
        belowIsLast,
        removeFrom,
        belowEnd + inserted,
        inserted);
  }

  /** Where an offset inside the block ends up after the move. */
  int moved(int offset) {
    return offset + shift;
  }

  /** The text to insert at {@link #insertAt}: the neighbour, plus the newline it may need. */
  String insertText(String text) {
    return (newlineBefore ? "\n" : "")
        + text.substring(copyFrom, copyTo)
        + (newlineAfter ? "\n" : "");
  }
}
