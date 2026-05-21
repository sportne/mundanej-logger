package io.github.mundanej.mlog.core.render;

import io.github.mundanej.mlog.api.LogEvent;
import io.github.mundanej.mlog.api.LogValue;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.config.TimestampMode;
import java.util.Map;
import java.util.Objects;

/** Deterministic JSON Lines renderer. */
public final class JsonLinesRenderer implements LogRenderer {
  private final MundaneLoggerConfig config;

  public JsonLinesRenderer(MundaneLoggerConfig config) {
    this.config = Objects.requireNonNull(config, "config");
  }

  @Override
  public String render(LogEvent event) {
    StringBuilder builder = new StringBuilder(256);
    builder.append('{');
    appendName(builder, "timestamp");
    if (config.timestampMode() == TimestampMode.EPOCH_MILLIS) {
      builder.append(event.timestamp().toEpochMilli());
    } else {
      JsonEscaper.appendQuoted(builder, event.timestamp().toString());
    }
    builder.append(',');
    appendName(builder, "level");
    JsonEscaper.appendQuoted(builder, event.level().name());
    builder.append(',');
    appendName(builder, "logger");
    JsonEscaper.appendQuoted(builder, event.loggerName());
    builder.append(',');
    appendName(builder, "thread");
    JsonEscaper.appendQuoted(builder, event.threadName());
    builder.append(',');
    appendName(builder, "event");
    JsonEscaper.appendQuoted(builder, event.eventName());
    builder.append(',');
    appendName(builder, "message");
    JsonEscaper.appendQuoted(builder, event.message());
    builder.append(',');
    appendName(builder, "throwable");
    ThrowableRenderer.appendJson(builder, event.throwable(), config.throwableMode());
    builder.append(',');
    appendName(builder, "context");
    appendFields(builder, event.contextFields());
    builder.append(',');
    appendName(builder, "fields");
    appendFields(builder, event.fields());
    builder.append("}\n");
    return builder.toString();
  }

  private static void appendFields(StringBuilder builder, Map<String, LogValue> fields) {
    builder.append('{');
    boolean first = true;
    for (Map.Entry<String, LogValue> entry : fields.entrySet()) {
      if (!first) {
        builder.append(',');
      }
      first = false;
      appendName(builder, entry.getKey());
      LogValueRenderer.appendJson(builder, entry.getValue());
    }
    builder.append('}');
  }

  private static void appendName(StringBuilder builder, String name) {
    JsonEscaper.appendQuoted(builder, name);
    builder.append(':');
  }
}
