package io.github.mundanej.mlog.testkit;

import io.github.mundanej.mlog.core.sink.LogSink;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** In-memory sink for tests. */
public final class CapturingLogSink implements LogSink {
  private final List<String> lines = new ArrayList<>();
  private boolean closed;

  @Override
  public synchronized void write(byte[] line) throws IOException {
    if (closed) {
      throw new IOException("capturing log sink is closed");
    }
    lines.add(new String(line, StandardCharsets.UTF_8));
  }

  @Override
  public synchronized void close() {
    closed = true;
  }

  public synchronized List<String> lines() {
    return List.copyOf(lines);
  }

  public synchronized String singleLine() {
    if (lines.size() != 1) {
      throw new AssertionError("expected one log line, got " + lines.size());
    }
    return lines.get(0);
  }
}
