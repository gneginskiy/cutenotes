package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InstanceChannelTest {

  @Test
  void aSecondStartMakesTheRunningAppShowItself(@TempDir Path data) throws Exception {
    CountDownLatch shown = new CountDownLatch(1);
    try (ServerSocket server = InstanceChannel.listen(data, shown::countDown)) {
      assertTrue(InstanceChannel.signal(data));
      assertTrue(shown.await(5, TimeUnit.SECONDS));
    }
  }

  @Test
  void strangersWithoutTheTokenAreIgnored(@TempDir Path data) throws Exception {
    AtomicInteger shown = new AtomicInteger();
    CountDownLatch handled = new CountDownLatch(1);
    Runnable onShow =
        () -> {
          shown.incrementAndGet();
          handled.countDown();
        };
    try (ServerSocket server = InstanceChannel.listen(data, onShow)) {
      try (Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort())) {
        OutputStream out = s.getOutputStream();
        out.write("guess show\n".getBytes(StandardCharsets.UTF_8));
      }
      assertTrue(InstanceChannel.signal(data));
      assertTrue(handled.await(5, TimeUnit.SECONDS));
    }
    assertEquals(1, shown.get(), "connections are served in order: the stranger was ignored");
  }

  @Test
  void nobodyListening(@TempDir Path data) throws Exception {
    assertFalse(InstanceChannel.signal(data), "no port file");
    ServerSocket closed = InstanceChannel.listen(data, () -> {});
    closed.close();
    assertFalse(InstanceChannel.signal(data), "app gone");
  }
}
