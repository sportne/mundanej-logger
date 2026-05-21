package io.github.mundanej.mlog.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Fluent builder for one log event. */
public interface LogEventBuilder {
  LogEventBuilder event(String eventName);

  LogEventBuilder message(String message);

  LogEventBuilder throwable(Throwable throwable);

  LogEventBuilder field(String name, String value);

  LogEventBuilder field(String name, boolean value);

  LogEventBuilder field(String name, int value);

  LogEventBuilder field(String name, long value);

  LogEventBuilder field(String name, double value);

  LogEventBuilder field(String name, BigDecimal value);

  LogEventBuilder field(String name, UUID value);

  LogEventBuilder field(String name, Instant value);

  LogEventBuilder fieldNull(String name);

  void emit();
}
