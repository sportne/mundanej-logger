package io.github.mundanej.mlog.core.render;

import io.github.mundanej.mlog.core.config.ThrowableMode;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.IdentityHashMap;
import java.util.Map;

final class ThrowableRenderer {
  private ThrowableRenderer() {}

  static void appendJson(StringBuilder builder, Throwable throwable, ThrowableMode mode) {
    if (throwable == null || mode == ThrowableMode.DISABLED) {
      builder.append("null");
      return;
    }
    appendThrowableJson(builder, throwable, mode, new IdentityHashMap<>());
  }

  static String renderText(Throwable throwable, ThrowableMode mode) {
    if (throwable == null || mode == ThrowableMode.DISABLED) {
      return "null";
    }
    StringBuilder builder = new StringBuilder();
    appendThrowableText(builder, throwable, mode, new IdentityHashMap<>());
    return TextEscaper.escape(builder.toString());
  }

  private static void appendThrowableJson(
      StringBuilder builder,
      Throwable throwable,
      ThrowableMode mode,
      Map<Throwable, Boolean> seen) {
    if (seen.put(throwable, Boolean.TRUE) != null) {
      builder.append("{\"cycle\":true}");
      return;
    }
    builder.append('{');
    builder.append("\"type\":");
    JsonEscaper.appendQuoted(builder, throwable.getClass().getName());
    builder.append(",\"message\":");
    JsonEscaper.appendQuoted(builder, throwable.getMessage());
    builder.append(",\"suppressed\":[");
    Throwable[] suppressed = throwable.getSuppressed();
    for (int index = 0; index < suppressed.length; index++) {
      if (index > 0) {
        builder.append(',');
      }
      appendThrowableJson(builder, suppressed[index], ThrowableMode.SUMMARY, seen);
    }
    builder.append(']');
    builder.append(",\"cause\":");
    Throwable cause = throwable.getCause();
    if (cause == null) {
      builder.append("null");
    } else {
      appendThrowableJson(builder, cause, ThrowableMode.SUMMARY, seen);
    }
    if (mode == ThrowableMode.STACKTRACE) {
      builder.append(",\"stackTrace\":");
      JsonEscaper.appendQuoted(builder, stackTrace(throwable));
    }
    builder.append('}');
  }

  private static void appendThrowableText(
      StringBuilder builder,
      Throwable throwable,
      ThrowableMode mode,
      Map<Throwable, Boolean> seen) {
    if (seen.put(throwable, Boolean.TRUE) != null) {
      builder.append("[cycle]");
      return;
    }
    builder.append(throwable.getClass().getName()).append(':').append(throwable.getMessage());
    for (Throwable suppressed : throwable.getSuppressed()) {
      builder.append(" suppressed=");
      appendThrowableText(builder, suppressed, ThrowableMode.SUMMARY, seen);
    }
    if (throwable.getCause() != null) {
      builder.append(" cause=");
      appendThrowableText(builder, throwable.getCause(), ThrowableMode.SUMMARY, seen);
    }
    if (mode == ThrowableMode.STACKTRACE) {
      builder.append(" stackTrace=").append(stackTrace(throwable));
    }
  }

  private static String stackTrace(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
}
