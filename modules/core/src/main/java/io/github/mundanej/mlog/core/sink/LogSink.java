package io.github.mundanej.mlog.core.sink;

import java.io.IOException;

/** Synchronous log event sink. */
public interface LogSink extends AutoCloseable {
  void write(byte[] line) throws IOException;

  @Override
  void close() throws IOException;
}
