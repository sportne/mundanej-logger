package io.github.mundanej.mlog.api;

/** Backend contract implemented by the core runtime. */
public interface LogBackend extends AutoCloseable {
  boolean isEnabled(String loggerName, LogLevel level);

  LogEventBuilder eventBuilder(String loggerName, LogLevel level);

  @Override
  void close();
}
