package dao;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import lombok.RequiredArgsConstructor;

/**
 * Guards a data folder against a second running instance: two windows auto-saving the same notes
 * would silently overwrite each other's edits. The OS releases the lock when the process dies, so a
 * crash never leaves the folder locked.
 */
@RequiredArgsConstructor
public final class InstanceLock implements AutoCloseable {

  static final String FILE_NAME = ".lock";

  private final FileChannel channel;

  /** Returns the held lock, or {@code null} when another instance already owns the folder. */
  public static InstanceLock tryAcquire(Path dir) {
    FileChannel channel;
    try {
      channel =
          FileChannel.open(
              dir.resolve(FILE_NAME), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    } catch (IOException e) {
      return new InstanceLock(null);
    }
    if (lock(channel) != null) {
      return new InstanceLock(channel);
    }
    closeQuietly(channel);
    return null;
  }

  private static FileLock lock(FileChannel channel) {
    try {
      return channel.tryLock();
    } catch (IOException | OverlappingFileLockException e) {
      return null;
    }
  }

  @Override
  public void close() {
    closeQuietly(channel);
  }

  private static void closeQuietly(FileChannel channel) {
    try {
      if (channel != null) {
        channel.close();
      }
    } catch (IOException e) {
      // nothing left to release
    }
  }
}
