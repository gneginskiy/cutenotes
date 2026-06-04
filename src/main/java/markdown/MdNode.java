package markdown;

/** A node of inline Markdown content: either styled {@link MdText} or an {@link MdImage}. */
public sealed interface MdNode permits MdText, MdImage {}
