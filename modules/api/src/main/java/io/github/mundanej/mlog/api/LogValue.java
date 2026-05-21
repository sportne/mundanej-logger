package io.github.mundanej.mlog.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A scalar structured logging value. */
public final class LogValue {
  /** Supported scalar kinds. */
  public enum Kind {
    NULL,
    STRING,
    BOOLEAN,
    INTEGER,
    LONG,
    DOUBLE,
    BIG_DECIMAL,
    UUID,
    INSTANT
  }

  private static final LogValue NULL = new LogValue(Kind.NULL, null);

  private final Kind kind;
  private final Object value;

  private LogValue(Kind kind, Object value) {
    this.kind = Objects.requireNonNull(kind, "kind");
    this.value = value;
  }

  public static LogValue nullValue() {
    return NULL;
  }

  public static LogValue of(String value) {
    return value == null ? NULL : new LogValue(Kind.STRING, value);
  }

  public static LogValue of(boolean value) {
    return new LogValue(Kind.BOOLEAN, Boolean.valueOf(value));
  }

  public static LogValue of(int value) {
    return new LogValue(Kind.INTEGER, Integer.valueOf(value));
  }

  public static LogValue of(long value) {
    return new LogValue(Kind.LONG, Long.valueOf(value));
  }

  public static LogValue of(double value) {
    return new LogValue(Kind.DOUBLE, Double.valueOf(value));
  }

  public static LogValue of(BigDecimal value) {
    return value == null ? NULL : new LogValue(Kind.BIG_DECIMAL, value);
  }

  public static LogValue of(UUID value) {
    return value == null ? NULL : new LogValue(Kind.UUID, value);
  }

  public static LogValue of(Instant value) {
    return value == null ? NULL : new LogValue(Kind.INSTANT, value);
  }

  public Kind kind() {
    return kind;
  }

  public Object value() {
    return value;
  }
}
