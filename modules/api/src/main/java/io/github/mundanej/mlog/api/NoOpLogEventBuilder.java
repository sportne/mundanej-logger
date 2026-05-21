package io.github.mundanej.mlog.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Disabled-level event builder. */
public final class NoOpLogEventBuilder implements LogEventBuilder {
  public static final NoOpLogEventBuilder INSTANCE = new NoOpLogEventBuilder();

  private NoOpLogEventBuilder() {}

  @Override
  public LogEventBuilder event(String eventName) {
    return this;
  }

  @Override
  public LogEventBuilder message(String message) {
    return this;
  }

  @Override
  public LogEventBuilder throwable(Throwable throwable) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, String value) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, boolean value) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, int value) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, long value) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, double value) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, BigDecimal value) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, UUID value) {
    return this;
  }

  @Override
  public LogEventBuilder field(String name, Instant value) {
    return this;
  }

  @Override
  public LogEventBuilder fieldNull(String name) {
    return this;
  }

  @Override
  public void emit() {}
}
