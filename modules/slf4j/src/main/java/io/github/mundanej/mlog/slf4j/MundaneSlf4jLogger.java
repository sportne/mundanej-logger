package io.github.mundanej.mlog.slf4j;

import io.github.mundanej.mlog.api.LogEventBuilder;
import io.github.mundanej.mlog.api.LogFieldNames;
import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.api.MundaneLogger;
import java.util.List;
import java.util.Map;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LoggingEventAware;

final class MundaneSlf4jLogger extends AbstractLogger implements LoggingEventAware {
  private static final long serialVersionUID = 1L;
  private static final String FQCN = MundaneSlf4jLogger.class.getName();

  private final transient MundaneLogger logger;
  private final transient MundaneMdcAdapter mdcAdapter;

  MundaneSlf4jLogger(String name, MundaneMdcAdapter mdcAdapter) {
    this.name = name;
    this.logger = MundaneLogger.get(name);
    this.mdcAdapter = mdcAdapter;
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return isTraceEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  @Override
  protected String getFullyQualifiedCallerName() {
    return FQCN;
  }

  @Override
  protected void handleNormalizedLoggingCall(
      Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
    FormattingTuple formatted = MessageFormatter.arrayFormat(messagePattern, arguments, throwable);
    emit(
        toLogLevel(level),
        formatted.getMessage(),
        formatted.getThrowable(),
        marker == null ? List.of() : List.of(marker),
        List.of());
  }

  @Override
  public void log(LoggingEvent event) {
    FormattingTuple formatted =
        MessageFormatter.arrayFormat(
            event.getMessage(), event.getArgumentArray(), event.getThrowable());
    emit(
        toLogLevel(event.getLevel()),
        formatted.getMessage(),
        formatted.getThrowable(),
        event.getMarkers(),
        event.getKeyValuePairs());
  }

  private void emit(
      LogLevel level,
      String message,
      Throwable throwable,
      List<Marker> markers,
      List<KeyValuePair> keyValuePairs) {
    LogEventBuilder builder = builder(level).message(message).throwable(throwable);
    if (markers != null && !markers.isEmpty()) {
      builder.field("slf4j.marker", markerNames(markers));
    }
    Map<String, String> mdc = mdcAdapter.getCopyOfContextMap();
    if (mdc != null) {
      for (Map.Entry<String, String> entry : mdc.entrySet()) {
        builder.field(safeFieldName("mdc.", entry.getKey()), entry.getValue());
      }
    }
    if (keyValuePairs != null) {
      for (KeyValuePair pair : keyValuePairs) {
        builder.field(safeFieldName("kv.", pair.key), String.valueOf(pair.value));
      }
    }
    builder.emit();
  }

  private LogEventBuilder builder(LogLevel level) {
    return switch (level) {
      case TRACE -> logger.trace();
      case DEBUG -> logger.debug();
      case INFO -> logger.info();
      case WARN -> logger.warn();
      case ERROR -> logger.error();
      case OFF -> logger.trace();
    };
  }

  private static LogLevel toLogLevel(Level level) {
    return switch (level) {
      case TRACE -> LogLevel.TRACE;
      case DEBUG -> LogLevel.DEBUG;
      case INFO -> LogLevel.INFO;
      case WARN -> LogLevel.WARN;
      case ERROR -> LogLevel.ERROR;
    };
  }

  private static String markerNames(List<Marker> markers) {
    StringBuilder builder = new StringBuilder();
    for (Marker marker : markers) {
      if (builder.length() > 0) {
        builder.append(',');
      }
      builder.append(marker.getName());
    }
    return builder.toString();
  }

  private static String safeFieldName(String prefix, String rawName) {
    StringBuilder builder = new StringBuilder(prefix);
    if (rawName != null) {
      for (int index = 0; index < rawName.length(); index++) {
        char character = rawName.charAt(index);
        if ((character >= 'A' && character <= 'Z')
            || (character >= 'a' && character <= 'z')
            || (character >= '0' && character <= '9')
            || character == '_'
            || character == '.'
            || character == '-') {
          builder.append(character);
        } else {
          builder.append('_');
        }
      }
    }
    String candidate = builder.length() > 128 ? builder.substring(0, 128) : builder.toString();
    return LogFieldNames.requireValid(candidate);
  }
}
