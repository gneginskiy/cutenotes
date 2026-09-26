import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import view.AppIcon;

/**
 * Writes the app icon for the native packages, drawn by the app's own {@link AppIcon}: {@code
 * cuteNotes.png} (Linux), {@code cuteNotes.ico} (Windows) and {@code cuteNotes.iconset} (turned
 * into .icns by macOS' iconutil).
 *
 * <p>Usage: java -cp cutenotes.jar:. IconFiles &lt;out dir&gt;
 */
public final class IconFiles {

  private static final int[] ICO_SIZES = {16, 24, 32, 48, 64, 128, 256};
  private static final int[] MAC_SIZES = {16, 32, 128, 256, 512};

  private IconFiles() {}

  public static void main(String[] args) throws IOException {
    Path out = Path.of(args[0]);
    Files.createDirectories(out);
    ImageIO.write(AppIcon.image(512), "png", out.resolve("cuteNotes.png").toFile());
    Files.write(out.resolve("cuteNotes.ico"), ico());
    Path iconset = Files.createDirectories(out.resolve("cuteNotes.iconset"));
    for (int size : MAC_SIZES) {
      ImageIO.write(
          AppIcon.image(size), "png", iconset.resolve("icon_" + size + "x" + size + ".png").toFile());
      ImageIO.write(
          AppIcon.image(size * 2),
          "png",
          iconset.resolve("icon_" + size + "x" + size + "@2x.png").toFile());
    }
  }

  /** An .ico file whose entries are PNG images (supported since Windows Vista). */
  static byte[] ico() throws IOException {
    List<byte[]> pngs = new ArrayList<>();
    for (int size : ICO_SIZES) {
      pngs.add(png(AppIcon.image(size)));
    }
    int headerSize = 6 + 16 * ICO_SIZES.length;
    ByteBuffer header = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
    header.putShort((short) 0).putShort((short) 1).putShort((short) ICO_SIZES.length);
    int offset = headerSize;
    for (int i = 0; i < ICO_SIZES.length; i++) {
      int size = ICO_SIZES[i];
      header.put((byte) (size >= 256 ? 0 : size)).put((byte) (size >= 256 ? 0 : size));
      header.put((byte) 0).put((byte) 0).putShort((short) 1).putShort((short) 32);
      header.putInt(pngs.get(i).length).putInt(offset);
      offset += pngs.get(i).length;
    }
    ByteArrayOutputStream all = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(all)) {
      out.write(header.array());
      for (byte[] png : pngs) {
        out.write(png);
      }
    }
    return all.toByteArray();
  }

  private static byte[] png(BufferedImage image) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bytes);
    return bytes.toByteArray();
  }
}
