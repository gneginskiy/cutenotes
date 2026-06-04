package markdown;

/** A run of text with inline styles. */
public record MdText(
    String text, boolean bold, boolean italic, boolean underline, boolean strike, boolean code)
    implements MdNode {}
