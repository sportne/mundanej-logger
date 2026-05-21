package io.github.mundanej.mlog.api;

import java.util.Objects;

/** Native mundane logger entrypoint. */
public final class MundaneLogger {
  private final String name;

  private MundaneLogger(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("logger name must not be blank");
    }
    this.name = name;
  }

  public static MundaneLogger get(String loggerName) {
    return new MundaneLogger(loggerName);
  }

  public static MundaneLogger get(Class<?> type) {
    Objects.requireNonNull(type, "type");
    return get(type.getName());
  }

  public String name() {
    return name;
  }

  public boolean isTraceEnabled() {
    return isEnabled(LogLevel.TRACE);
  }

  public boolean isDebugEnabled() {
    return isEnabled(LogLevel.DEBUG);
  }

  public boolean isInfoEnabled() {
    return isEnabled(LogLevel.INFO);
  }

  public boolean isWarnEnabled() {
    return isEnabled(LogLevel.WARN);
  }

  public boolean isErrorEnabled() {
    return isEnabled(LogLevel.ERROR);
  }

  public LogEventBuilder trace() {
    return builder(LogLevel.TRACE);
  }

  public LogEventBuilder debug() {
    return builder(LogLevel.DEBUG);
  }

  public LogEventBuilder info() {
    return builder(LogLevel.INFO);
  }

  public LogEventBuilder warn() {
    return builder(LogLevel.WARN);
  }

  public LogEventBuilder error() {
    return builder(LogLevel.ERROR);
  }

  private boolean isEnabled(LogLevel level) {
    return MundaneLogBackend.current().isEnabled(name, level);
  }

  private LogEventBuilder builder(LogLevel level) {
    LogBackend backend = MundaneLogBackend.current();
    if (!backend.isEnabled(name, level)) {
      return NoOpLogEventBuilder.INSTANCE;
    }
    return backend.eventBuilder(name, level);
  }
}
