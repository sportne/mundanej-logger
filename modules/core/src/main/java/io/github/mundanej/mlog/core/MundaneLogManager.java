package io.github.mundanej.mlog.core;

import io.github.mundanej.mlog.api.LogBackend;
import io.github.mundanej.mlog.api.MundaneLogBackend;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.config.OutputFormat;
import io.github.mundanej.mlog.core.config.SinkType;
import io.github.mundanej.mlog.core.render.JsonLinesRenderer;
import io.github.mundanej.mlog.core.render.LogRenderer;
import io.github.mundanej.mlog.core.render.PlainTextRenderer;
import io.github.mundanej.mlog.core.sink.FileLogSink;
import io.github.mundanej.mlog.core.sink.LogSink;
import io.github.mundanej.mlog.core.sink.OutputStreamLogSink;
import java.io.IOException;
import java.time.Clock;
import java.util.Objects;

/** Explicit core runtime bootstrap and configuration API. */
public final class MundaneLogManager {
  private MundaneLogManager() {}

  public static LogBackend installDefault() {
    return install(MundaneLoggerConfig.loadDefault());
  }

  public static LogBackend install(MundaneLoggerConfig config) {
    try {
      return install(config, createSink(config), Clock.systemUTC());
    } catch (IOException exception) {
      throw new IllegalArgumentException("failed to initialize logger sink", exception);
    }
  }

  public static LogBackend install(MundaneLoggerConfig config, LogSink sink) {
    return install(config, sink, Clock.systemUTC());
  }

  public static LogBackend install(MundaneLoggerConfig config, LogSink sink, Clock clock) {
    CoreLogBackend backend =
        new CoreLogBackend(
            config, createRenderer(config), Objects.requireNonNull(sink, "sink"), clock);
    MundaneLogBackend.install(backend);
    return backend;
  }

  public static void reset() {
    MundaneLogBackend.resetToNoOp();
  }

  private static LogRenderer createRenderer(MundaneLoggerConfig config) {
    return config.format() == OutputFormat.TEXT
        ? new PlainTextRenderer(config)
        : new JsonLinesRenderer(config);
  }

  private static LogSink createSink(MundaneLoggerConfig config) throws IOException {
    if (config.sinkType() == SinkType.STDOUT) {
      return new OutputStreamLogSink(System.out);
    }
    if (config.sinkType() == SinkType.FILE) {
      return new FileLogSink(config.filePath(), config.fileMode());
    }
    return new OutputStreamLogSink(System.err);
  }
}
