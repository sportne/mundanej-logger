package io.github.mundanej.mlog.core;

import io.github.mundanej.mlog.api.LogContext;
import io.github.mundanej.mlog.api.LogEvent;
import io.github.mundanej.mlog.api.LogEventBuilder;
import io.github.mundanej.mlog.api.LogFieldNames;
import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.api.LogValue;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class CoreLogEventBuilder implements LogEventBuilder {
  private final CoreLogBackend backend;
  private final String loggerName;
  private final LogLevel level;
  private final Map<String, LogValue> fields = new LinkedHashMap<>();
  private String eventName;
  private String message;
  private Throwable throwable;
  private boolean emitted;

  CoreLogEventBuilder(CoreLogBackend backend, String loggerName, LogLevel level) {
    this.backend = Objects.requireNonNull(backend, "backend");
    this.loggerName = Objects.requireNonNull(loggerName, "loggerName");
    this.level = Objects.requireNonNull(level, "level");
  }

  @Override
  public LogEventBuilder event(String eventName) {
    this.eventName = eventName;
    return this;
  }

  @Override
  public LogEventBuilder message(String message) {
    this.message = message;
    return this;
  }

  @Override
  public LogEventBuilder throwable(Throwable throwable) {
    this.throwable = throwable;
    return this;
  }

  @Override
  public LogEventBuilder field(String name, String value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder field(String name, boolean value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder field(String name, int value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder field(String name, long value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder field(String name, double value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder field(String name, BigDecimal value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder field(String name, UUID value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder field(String name, Instant value) {
    return put(name, LogValue.of(value));
  }

  @Override
  public LogEventBuilder fieldNull(String name) {
    return put(name, LogValue.nullValue());
  }

  @Override
  public void emit() {
    if (emitted) {
      throw new IllegalStateException("log event builder may only be emitted once");
    }
    emitted = true;
    Thread currentThread = Thread.currentThread();
    String threadName = backend.config().includeThread() ? currentThread.getName() : null;
    backend.emit(
        new LogEvent(
            backend.clock().instant(),
            level,
            loggerName,
            threadName,
            eventName,
            message,
            throwable,
            LogContext.capture(),
            fields));
  }

  private LogEventBuilder put(String name, LogValue value) {
    fields.put(LogFieldNames.requireValid(name), Objects.requireNonNull(value, "value"));
    return this;
  }
}
