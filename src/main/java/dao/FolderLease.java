package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Who is using a notes folder right now, for folders shared between computers through a sync
 * service: the file lock only guards one computer. Each running app renews {@code .cutenotes-owner}
 * (host and time) every minute; a fresh lease of another host warns.
 */
public final class FolderLease {

  static final String FILE = ".cutenotes-owner";
  public static final Duration RENEW = Duration.ofMinutes(1);
  static final Duration FRESH = Duration.ofMinutes(3);

  /** Another computer's recent use of the folder. */
  public record Holder(String host, Instant since) {}

  private final Path file;
  private final String host;

  public FolderLease(Path dataDir, String host) {
    this.file = dataDir.resolve(FILE);
    this.host = host;
  }

  /** Another host that used the folder within the last minutes, or {@code null}. */
  public Holder otherHolder(Instant now) {
    try {
      String[] parts = Files.readString(file, StandardCharsets.UTF_8).trim().split("\t");
      Instant at = Instant.ofEpochMilli(Long.parseLong(parts[1]));
      boolean fresh = at.isAfter(now.minus(FRESH));
      return parts.length == 2 && fresh && !parts[0].equals(host) ? new Holder(parts[0], at) : null;
    } catch (Exception e) {
      return null;
    }
  }

  /** This computer's name, as other computers sharing the folder will see it. */
  public static String localHost() {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      return System.getProperty("user.name", "?") + "'s computer";
    }
  }

  /** Renews the lease every minute on a virtual thread for as long as the app runs. */
  public void keepRenewing() {
    Thread.ofVirtual()
        .name("folder-lease")
        .start(
            () -> {
              while (!Thread.currentThread().isInterrupted()) {
                renew(Instant.now());
                try {
                  Thread.sleep(RENEW);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }
            });
  }

  /** Claims (or keeps claiming) the folder for this host. */
  public void renew(Instant now) {
    try {
      AtomicFiles.write(file, host + "\t" + now.toEpochMilli());
    } catch (Exception e) {
      // a read-only or vanished folder: nothing to announce
    }
  }
}
