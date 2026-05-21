package io.github.mundanej.mlog.core.render;

import io.github.mundanej.mlog.api.LogEvent;
import io.github.mundanej.mlog.api.LogValue;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.config.TimestampMode;
import java.util.Map;
import java.util.Objects;

/** Deterministic one-line plain text renderer. */
public final class PlainTextRenderer implements LogRenderer {
  private final MundaneLoggerConfig config;

  public PlainTextRenderer(MundaneLoggerConfig config) {
    this.config = Objects.requireNonNull(config, "config");
  }

  @Override
  public String render(LogEvent event) {
    StringBuilder builder = new StringBuilder(192);
    if (config.timestampMode() == TimestampMode.EPOCH_MILLIS) {
      builder.append(event.timestamp().toEpochMilli());
    } else {
      builder.append(event.timestamp());
    }
    builder.append(" level=").append(event.level().name());
    builder.append(" logger=").append(TextEscaper.escape(event.loggerName()));
    if (event.threadName() != null) {
      builder.append(" thread=").append(TextEscaper.escape(event.threadName()));
    }
    if (event.eventName() != null) {
      builder.append(" event=").append(TextEscaper.escape(event.eventName()));
    }
    if (event.message() != null) {
      builder.append(" message=").append(TextEscaper.escape(event.message()));
    }
    appendFields(builder, " context.", event.contextFields());
    appendFields(builder, " field.", event.fields());
    if (event.throwable() != null) {
      builder
          .append(" throwable=")
          .append(ThrowableRenderer.renderText(event.throwable(), config.throwableMode()));
    }
    builder.append('\n');
    return builder.toString();
  }

  private static void appendFields(
      StringBuilder builder, String prefix, Map<String, LogValue> fields) {
    for (Map.Entry<String, LogValue> entry : fields.entrySet()) {
      builder.append(prefix);
      builder.append(TextEscaper.escape(entry.getKey()));
      builder.append('=');
      builder.append(LogValueRenderer.renderText(entry.getValue()));
    }
  }
}
