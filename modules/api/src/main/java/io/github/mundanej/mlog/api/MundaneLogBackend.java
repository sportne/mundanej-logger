package io.github.mundanej.mlog.api;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** Global backend registry used without service loading or dynamic discovery. */
public final class MundaneLogBackend {
  private static final LogBackend NO_OP_BACKEND =
      new LogBackend() {
        @Override
        public boolean isEnabled(String loggerName, LogLevel level) {
          return false;
        }

        @Override
        public LogEventBuilder eventBuilder(String loggerName, LogLevel level) {
          return NoOpLogEventBuilder.INSTANCE;
        }

        @Override
        public void close() {}
      };

  private static final AtomicReference<LogBackend> CURRENT = new AtomicReference<>(NO_OP_BACKEND);

  private MundaneLogBackend() {}

  public static LogBackend current() {
    return CURRENT.get();
  }

  public static boolean isNoOpInstalled() {
    return CURRENT.get() == NO_OP_BACKEND;
  }

  public static void install(LogBackend backend) {
    CURRENT.set(Objects.requireNonNull(backend, "backend"));
  }

  public static void resetToNoOp() {
    CURRENT.set(NO_OP_BACKEND);
  }
}
