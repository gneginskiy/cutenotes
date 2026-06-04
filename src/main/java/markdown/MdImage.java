package markdown;

/** An inline image reference; {@code width}/{@code height} of 0 mean "natural size". */
public record MdImage(String path, int width, int height) implements MdNode {}
