package io.github.mundanej.mlog.api;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable log event passed from the API layer to a backend. */
public final class LogEvent {
  private final Instant timestamp;
  private final LogLevel level;
  private final String loggerName;
  private final String threadName;
  private final String eventName;
  private final String message;
  private final Throwable throwable;
  private final Map<String, LogValue> contextFields;
  private final Map<String, LogValue> fields;

  public LogEvent(
      Instant timestamp,
      LogLevel level,
      String loggerName,
      String threadName,
      String eventName,
      String message,
      Throwable throwable,
      Map<String, LogValue> contextFields,
      Map<String, LogValue> fields) {
    this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    this.level = Objects.requireNonNull(level, "level");
    this.loggerName = Objects.requireNonNull(loggerName, "loggerName");
    this.threadName = threadName;
    this.eventName = eventName;
    this.message = message;
    this.throwable = throwable;
    this.contextFields = immutableCopy(contextFields);
    this.fields = immutableCopy(fields);
  }

  public Instant timestamp() {
    return timestamp;
  }

  public LogLevel level() {
    return level;
  }

  public String loggerName() {
    return loggerName;
  }

  public String threadName() {
    return threadName;
  }

  public String eventName() {
    return eventName;
  }

  public String message() {
    return message;
  }

  public Throwable throwable() {
    return throwable;
  }

  public Map<String, LogValue> contextFields() {
    return immutableCopy(contextFields);
  }

  public Map<String, LogValue> fields() {
    return immutableCopy(fields);
  }

  private static Map<String, LogValue> immutableCopy(Map<String, LogValue> source) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(source));
  }
}
