package io.github.mundanej.mlog.core;

import io.github.mundanej.mlog.api.LogBackend;
import io.github.mundanej.mlog.api.LogEvent;
import io.github.mundanej.mlog.api.LogEventBuilder;
import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.api.MundaneLogBackend;
import io.github.mundanej.mlog.api.NoOpLogEventBuilder;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.render.LogRenderer;
import io.github.mundanej.mlog.core.sink.LogSink;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Core synchronous backend. */
public final class CoreLogBackend implements LogBackend {
  private final MundaneLoggerConfig config;
  private final LogRenderer renderer;
  private final LogSink sink;
  private final Clock clock;
  private final AtomicBoolean sinkFailed = new AtomicBoolean();

  public CoreLogBackend(
      MundaneLoggerConfig config, LogRenderer renderer, LogSink sink, Clock clock) {
    this.config = Objects.requireNonNull(config, "config");
    this.renderer = Objects.requireNonNull(renderer, "renderer");
    this.sink = Objects.requireNonNull(sink, "sink");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public boolean isEnabled(String loggerName, LogLevel level) {
    Objects.requireNonNull(loggerName, "loggerName");
    Objects.requireNonNull(level, "level");
    return level.isEnabledBy(config.levelFor(loggerName));
  }

  @Override
  public LogEventBuilder eventBuilder(String loggerName, LogLevel level) {
    if (!isEnabled(loggerName, level)) {
      return NoOpLogEventBuilder.INSTANCE;
    }
    return new CoreLogEventBuilder(this, loggerName, level);
  }

  void emit(LogEvent event) {
    if (sinkFailed.get()) {
      return;
    }
    String rendered = renderer.render(event);
    try {
      sink.write(rendered.getBytes(StandardCharsets.UTF_8));
    } catch (IOException exception) {
      sinkFailed.set(true);
      System.err.println("mundane-logger sink failure: " + exception.getMessage());
    }
  }

  MundaneLoggerConfig config() {
    return config;
  }

  Clock clock() {
    return clock;
  }

  @Override
  public void close() {
    try {
      sink.close();
    } catch (IOException exception) {
      sinkFailed.set(true);
      System.err.println("mundane-logger sink close failure: " + exception.getMessage());
    } finally {
      if (MundaneLogBackend.current() == this) {
        MundaneLogBackend.resetToNoOp();
      }
    }
  }
}
