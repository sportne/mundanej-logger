package io.github.mundanej.mlog.core.sink;

import io.github.mundanej.mlog.core.config.FileMode;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/** Synchronized file sink. */
public final class FileLogSink implements LogSink {
  private final OutputStream output;
  private boolean closed;

  public FileLogSink(Path path, FileMode mode) throws IOException {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(mode, "mode");
    Path parent = path.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    if (mode == FileMode.TRUNCATE) {
      output =
          Files.newOutputStream(
              path,
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.WRITE);
    } else {
      output =
          Files.newOutputStream(
              path, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }
  }

  @Override
  public synchronized void write(byte[] line) throws IOException {
    if (closed) {
      throw new IOException("log file sink is closed");
    }
    output.write(line);
    output.flush();
  }

  @Override
  public synchronized void close() throws IOException {
    if (!closed) {
      closed = true;
      output.close();
    }
  }
}
