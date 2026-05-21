package io.github.mundanej.mlog.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Lightweight non-inheritable thread-local structured context. */
public final class LogContext {
  private static final ThreadLocal<Map<String, LogValue>> CONTEXT =
      new ThreadLocal<>() {
        @Override
        protected Map<String, LogValue> initialValue() {
          return new LinkedHashMap<>();
        }
      };

  private LogContext() {}

  public static Scope put(String key, LogValue value) {
    LogFieldNames.requireValid(key);
    Objects.requireNonNull(value, "value");
    Map<String, LogValue> fields = CONTEXT.get();
    boolean hadPrevious = fields.containsKey(key);
    LogValue previous = fields.put(key, value);
    return new Scope(key, hadPrevious, previous);
  }

  public static Map<String, LogValue> capture() {
    Map<String, LogValue> fields = CONTEXT.get();
    if (fields.isEmpty()) {
      return Map.of();
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(fields));
  }

  public static void clear() {
    CONTEXT.remove();
  }

  /** Scope that restores the previous context value on close. */
  public static final class Scope implements AutoCloseable {
    private final String key;
    private final boolean hadPrevious;
    private final LogValue previous;
    private boolean closed;

    private Scope(String key, boolean hadPrevious, LogValue previous) {
      this.key = key;
      this.hadPrevious = hadPrevious;
      this.previous = previous;
    }

    @Override
    public void close() {
      if (closed) {
        return;
      }
      Map<String, LogValue> fields = CONTEXT.get();
      if (hadPrevious) {
        fields.put(key, previous);
      } else {
        fields.remove(key);
      }
      if (fields.isEmpty()) {
        CONTEXT.remove();
      }
      closed = true;
    }
  }
}
