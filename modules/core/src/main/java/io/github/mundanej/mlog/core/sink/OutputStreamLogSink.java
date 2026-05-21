package io.github.mundanej.mlog.core.sink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/** Synchronized stdout/stderr sink. */
public final class OutputStreamLogSink implements LogSink {
  private final OutputStream output;

  public OutputStreamLogSink(OutputStream output) {
    this.output = Objects.requireNonNull(output, "output");
  }

  @Override
  public synchronized void write(byte[] line) throws IOException {
    output.write(line);
    output.flush();
  }

  @Override
  public void close() throws IOException {
    output.flush();
  }
}
