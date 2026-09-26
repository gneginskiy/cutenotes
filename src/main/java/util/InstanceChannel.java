package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lets a second start of the app hand over to the running one, which then shows its window (a
 * global hotkey bound to the app, a dock or launcher click). The running app listens on a local
 * port written, with a random token, to {@code <data>/.cutenotes-port}.
 */
public final class InstanceChannel {

  public static final String FILE = ".cutenotes-port";
  private static final String SHOW = "show";
  private static final int TIMEOUT_MS = 1_000;
  private static final Logger LOG = Logger.getLogger(InstanceChannel.class.getName());

  private InstanceChannel() {}

  /** Starts listening; {@code onShow} runs (on a virtual thread) when another start asks. */
  public static ServerSocket listen(Path dataDir, Runnable onShow) throws IOException {
    ServerSocket server = new ServerSocket(0, 4, InetAddress.getLoopbackAddress());
    byte[] random = new byte[16];
    new SecureRandom().nextBytes(random);
    String token = HexFormat.of().formatHex(random);
    Files.writeString(
        dataDir.resolve(FILE), server.getLocalPort() + " " + token, StandardCharsets.UTF_8);
    Thread.ofVirtual().name("instance-channel").start(() -> serve(server, token, onShow));
    return server;
  }

  /** Asks the running app to show itself; false when none answered. */
  public static boolean signal(Path dataDir) {
    try {
      String[] parts =
          Files.readString(dataDir.resolve(FILE), StandardCharsets.UTF_8).trim().split(" ");
      try (Socket socket = new Socket()) {
        socket.connect(
            new InetSocketAddress(InetAddress.getLoopbackAddress(), Integer.parseInt(parts[0])),
            TIMEOUT_MS);
        OutputStream out = socket.getOutputStream();
        out.write((parts[1] + " " + SHOW + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
        return true;
      }
    } catch (IOException | RuntimeException e) {
      return false;
    }
  }

  private static void serve(ServerSocket server, String token, Runnable onShow) {
    while (!server.isClosed()) {
      try (Socket client = server.accept()) {
        client.setSoTimeout(TIMEOUT_MS);
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        if ((token + " " + SHOW).equals(in.readLine())) {
          onShow.run();
        }
      } catch (IOException e) {
        LOG.log(Level.FINE, "instance channel", e);
      }
    }
  }
}
