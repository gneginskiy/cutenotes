package view;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Outlines of the {@link VectorIcon} glyphs, drawn on a 16×16 grid. */
final class IconShapes {

  static final float GRID = 16f;

  private IconShapes() {}

  /** The stroked outline of {@code kind}. */
  static Shape outline(VectorIcon.Kind kind) {
    return switch (kind) {
      case CLOSE -> join(line(4.5f, 4.5f, 11.5f, 11.5f), line(11.5f, 4.5f, 4.5f, 11.5f));
      case PLUS -> join(line(8, 3.5f, 8, 12.5f), line(3.5f, 8, 12.5f, 8));
      case CHEVRON_UP -> line(4, 10, 8, 6, 12, 10);
      case CHEVRON_DOWN -> line(4, 6, 8, 10, 12, 6);
      case SEARCH -> join(new Ellipse2D.Float(2.5f, 2.5f, 8.5f, 8.5f), line(10, 10, 13.5f, 13.5f));
      case PIN, PIN_ON -> join(pinBody(), line(5, 2.5f, 11, 2.5f), line(8, 9.5f, 8, 14));
      case NOTE -> join(noteBody(), line(9.5f, 2.5f, 9.5f, 5.5f, 12.5f, 5.5f));
      case FOLDER -> folderBody();
    };
  }

  /** The filled part of {@code kind}, or {@code null} for a pure outline. */
  static Shape fill(VectorIcon.Kind kind) {
    return kind == VectorIcon.Kind.PIN_ON ? pinBody() : null;
  }

  private static Path2D pinBody() {
    return closed(line(6.2f, 2.5f, 6.2f, 6.5f, 4, 9.5f, 12, 9.5f, 9.8f, 6.5f, 9.8f, 2.5f));
  }

  private static Path2D noteBody() {
    return closed(line(4, 2.5f, 9.5f, 2.5f, 12.5f, 5.5f, 12.5f, 13.5f, 4, 13.5f));
  }

  private static Path2D folderBody() {
    return closed(line(2.5f, 4, 6.5f, 4, 8, 5.5f, 13.5f, 5.5f, 13.5f, 12.5f, 2.5f, 12.5f));
  }

  /** A polyline through the given x,y pairs. */
  private static Path2D line(float... xy) {
    Path2D.Float p = new Path2D.Float();
    p.moveTo(xy[0], xy[1]);
    for (int i = 2; i < xy.length; i += 2) {
      p.lineTo(xy[i], xy[i + 1]);
    }
    return p;
  }

  private static Path2D closed(Path2D p) {
    p.closePath();
    return p;
  }

  private static Shape join(Shape... parts) {
    Path2D.Float all = new Path2D.Float();
    for (Shape part : parts) {
      all.append(part, false);
    }
    return all;
  }
}
