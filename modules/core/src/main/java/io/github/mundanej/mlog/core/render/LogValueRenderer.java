package io.github.mundanej.mlog.core.render;

import io.github.mundanej.mlog.api.LogValue;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

final class LogValueRenderer {
  private LogValueRenderer() {}

  static void appendJson(StringBuilder builder, LogValue value) {
    if (value == null || value.kind() == LogValue.Kind.NULL) {
      builder.append("null");
      return;
    }
    switch (value.kind()) {
      case STRING -> JsonEscaper.appendQuoted(builder, (String) value.value());
      case BOOLEAN -> builder.append(((Boolean) value.value()).booleanValue());
      case INTEGER -> builder.append(((Integer) value.value()).intValue());
      case LONG -> builder.append(((Long) value.value()).longValue());
      case DOUBLE -> appendDouble(builder, ((Double) value.value()).doubleValue());
      case BIG_DECIMAL -> builder.append(((BigDecimal) value.value()).toPlainString());
      case UUID -> JsonEscaper.appendQuoted(builder, ((UUID) value.value()).toString());
      case INSTANT -> JsonEscaper.appendQuoted(builder, ((Instant) value.value()).toString());
      case NULL -> builder.append("null");
    }
  }

  static String renderText(LogValue value) {
    if (value == null || value.kind() == LogValue.Kind.NULL) {
      return "null";
    }
    return switch (value.kind()) {
      case STRING -> TextEscaper.escape((String) value.value());
      case BOOLEAN, INTEGER, LONG, BIG_DECIMAL -> value.value().toString();
      case DOUBLE -> {
        double doubleValue = ((Double) value.value()).doubleValue();
        yield Double.isFinite(doubleValue)
            ? Double.toString(doubleValue)
            : TextEscaper.escape(Double.toString(doubleValue));
      }
      case UUID, INSTANT -> value.value().toString();
      case NULL -> "null";
    };
  }

  private static void appendDouble(StringBuilder builder, double value) {
    if (Double.isFinite(value)) {
      builder.append(value);
    } else {
      JsonEscaper.appendQuoted(builder, Double.toString(value));
    }
  }
}
