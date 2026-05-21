package io.github.mundanej.mlog.api;

/** Standard logging levels ordered from most verbose to disabled. */
public enum LogLevel {
  TRACE(0),
  DEBUG(10),
  INFO(20),
  WARN(30),
  ERROR(40),
  OFF(50);

  private final int severity;

  LogLevel(int severity) {
    this.severity = severity;
  }

  /** Returns whether this event level is enabled by the supplied threshold. */
  public boolean isEnabledBy(LogLevel threshold) {
    return threshold != OFF && severity >= threshold.severity;
  }
}
