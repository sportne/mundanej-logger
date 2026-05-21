package io.github.mundanej.mlog.core.config;

import io.github.mundanej.mlog.api.LogLevel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/** Immutable runtime configuration. */
public final class MundaneLoggerConfig {
  private final LogLevel rootLevel;
  private final Map<String, LogLevel> loggerLevels;
  private final OutputFormat format;
  private final SinkType sinkType;
  private final Path filePath;
  private final FileMode fileMode;
  private final boolean includeThread;
  private final ThrowableMode throwableMode;
  private final TimestampMode timestampMode;

  private MundaneLoggerConfig(Builder builder) {
    this.rootLevel = Objects.requireNonNull(builder.rootLevel, "rootLevel");
    this.loggerLevels = Collections.unmodifiableMap(new LinkedHashMap<>(builder.loggerLevels));
    this.format = Objects.requireNonNull(builder.format, "format");
    this.sinkType = Objects.requireNonNull(builder.sinkType, "sinkType");
    this.filePath = builder.filePath;
    this.fileMode = Objects.requireNonNull(builder.fileMode, "fileMode");
    this.includeThread = builder.includeThread;
    this.throwableMode = Objects.requireNonNull(builder.throwableMode, "throwableMode");
    this.timestampMode = Objects.requireNonNull(builder.timestampMode, "timestampMode");
    if (sinkType == SinkType.FILE && filePath == null) {
      throw new IllegalArgumentException("file.path is required when sink=file");
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static MundaneLoggerConfig defaults() {
    return builder().build();
  }

  public static MundaneLoggerConfig loadDefault() {
    return load(System.getProperties(), System.getenv());
  }

  public static MundaneLoggerConfig load(
      Properties systemProperties, Map<String, String> environment) {
    Objects.requireNonNull(systemProperties, "systemProperties");
    Objects.requireNonNull(environment, "environment");
    Builder builder = builder();
    String configuredFile =
        firstNonBlank(
            systemProperties.getProperty("mundane.logger.configFile"),
            environment.get("MUNDANE_LOGGER_CONFIG_FILE"));
    if (configuredFile != null) {
      applyPropertiesFile(builder, Path.of(configuredFile));
    }
    applyEnvironment(builder, environment);
    applySystemProperties(builder, systemProperties);
    return builder.build();
  }

  public LogLevel rootLevel() {
    return rootLevel;
  }

  public Map<String, LogLevel> loggerLevels() {
    return loggerLevels;
  }

  public OutputFormat format() {
    return format;
  }

  public SinkType sinkType() {
    return sinkType;
  }

  public Path filePath() {
    return filePath;
  }

  public FileMode fileMode() {
    return fileMode;
  }

  public boolean includeThread() {
    return includeThread;
  }

  public ThrowableMode throwableMode() {
    return throwableMode;
  }

  public TimestampMode timestampMode() {
    return timestampMode;
  }

  public LogLevel levelFor(String loggerName) {
    LogLevel exact = loggerLevels.get(loggerName);
    if (exact != null) {
      return exact;
    }
    String candidate = loggerName;
    while (true) {
      int dot = candidate.lastIndexOf('.');
      if (dot < 0) {
        return rootLevel;
      }
      candidate = candidate.substring(0, dot);
      LogLevel inherited = loggerLevels.get(candidate);
      if (inherited != null) {
        return inherited;
      }
    }
  }

  private static void applyPropertiesFile(Builder builder, Path path) {
    Properties properties = new Properties();
    try (InputStream input = Files.newInputStream(path)) {
      properties.load(input);
    } catch (IOException exception) {
      throw new IllegalArgumentException("failed to read logger config file: " + path, exception);
    }
    applySystemProperties(builder, properties);
  }

  private static void applyEnvironment(Builder builder, Map<String, String> environment) {
    String rootLevel = environment.get("MUNDANE_LOGGER_ROOT_LEVEL");
    if (rootLevel != null && !rootLevel.isBlank()) {
      builder.rootLevel(parseEnum("MUNDANE_LOGGER_ROOT_LEVEL", rootLevel, LogLevel.class));
    }
    String format = environment.get("MUNDANE_LOGGER_FORMAT");
    if (format != null && !format.isBlank()) {
      builder.format(parseEnum("MUNDANE_LOGGER_FORMAT", format, OutputFormat.class));
    }
    String sink = environment.get("MUNDANE_LOGGER_SINK");
    if (sink != null && !sink.isBlank()) {
      builder.sinkType(parseEnum("MUNDANE_LOGGER_SINK", sink, SinkType.class));
    }
    String fileMode = environment.get("MUNDANE_LOGGER_FILE_MODE");
    if (fileMode != null && !fileMode.isBlank()) {
      builder.fileMode(parseEnum("MUNDANE_LOGGER_FILE_MODE", fileMode, FileMode.class));
    }
    String throwableMode = environment.get("MUNDANE_LOGGER_THROWABLE");
    if (throwableMode != null && !throwableMode.isBlank()) {
      builder.throwableMode(
          parseEnum("MUNDANE_LOGGER_THROWABLE", throwableMode, ThrowableMode.class));
    }
    String timestampMode = environment.get("MUNDANE_LOGGER_TIMESTAMP");
    if (timestampMode != null && !timestampMode.isBlank()) {
      builder.timestampMode(
          parseEnum("MUNDANE_LOGGER_TIMESTAMP", timestampMode, TimestampMode.class));
    }
    String filePath = environment.get("MUNDANE_LOGGER_FILE_PATH");
    if (filePath != null && !filePath.isBlank()) {
      builder.filePath(Path.of(filePath));
    }
    String includeThread = environment.get("MUNDANE_LOGGER_INCLUDE_THREAD");
    if (includeThread != null && !includeThread.isBlank()) {
      builder.includeThread(parseBoolean("MUNDANE_LOGGER_INCLUDE_THREAD", includeThread));
    }
    String loggerPrefix = "MUNDANE_LOGGER_LEVEL_";
    for (Map.Entry<String, String> entry : environment.entrySet()) {
      String key = entry.getKey();
      if (key.equals(loggerPrefix)) {
        throw new IllegalArgumentException("logger name must not be blank for " + key);
      }
      if (key.startsWith(loggerPrefix) && key.length() > loggerPrefix.length()) {
        String loggerName =
            key.substring(loggerPrefix.length()).replace('_', '.').toLowerCase(Locale.ROOT);
        builder.level(loggerName, parseEnum(key, entry.getValue(), LogLevel.class));
      }
    }
  }

  private static void applySystemProperties(Builder builder, Properties properties) {
    String rootLevel = properties.getProperty("mundane.logger.rootLevel");
    if (rootLevel != null && !rootLevel.isBlank()) {
      builder.rootLevel(parseEnum("mundane.logger.rootLevel", rootLevel, LogLevel.class));
    }
    String format = properties.getProperty("mundane.logger.format");
    if (format != null && !format.isBlank()) {
      builder.format(parseEnum("mundane.logger.format", format, OutputFormat.class));
    }
    String sink = properties.getProperty("mundane.logger.sink");
    if (sink != null && !sink.isBlank()) {
      builder.sinkType(parseEnum("mundane.logger.sink", sink, SinkType.class));
    }
    String fileMode = properties.getProperty("mundane.logger.file.mode");
    if (fileMode != null && !fileMode.isBlank()) {
      builder.fileMode(parseEnum("mundane.logger.file.mode", fileMode, FileMode.class));
    }
    String throwableMode = properties.getProperty("mundane.logger.throwable");
    if (throwableMode != null && !throwableMode.isBlank()) {
      builder.throwableMode(
          parseEnum("mundane.logger.throwable", throwableMode, ThrowableMode.class));
    }
    String timestampMode = properties.getProperty("mundane.logger.timestamp");
    if (timestampMode != null && !timestampMode.isBlank()) {
      builder.timestampMode(
          parseEnum("mundane.logger.timestamp", timestampMode, TimestampMode.class));
    }
    String filePath = properties.getProperty("mundane.logger.file.path");
    if (filePath != null && !filePath.isBlank()) {
      builder.filePath(Path.of(filePath));
    }
    String includeThread = properties.getProperty("mundane.logger.includeThread");
    if (includeThread != null && !includeThread.isBlank()) {
      builder.includeThread(parseBoolean("mundane.logger.includeThread", includeThread));
    }
    for (String name : properties.stringPropertyNames()) {
      String prefix = "mundane.logger.level.";
      if (name.startsWith(prefix)) {
        String loggerName = name.substring(prefix.length());
        if (loggerName.isBlank()) {
          throw new IllegalArgumentException("logger name must not be blank for " + name);
        }
        builder.level(loggerName, parseEnum(name, properties.getProperty(name), LogLevel.class));
      }
    }
  }

  private static <T extends Enum<T>> T parseEnum(String key, String value, Class<T> type) {
    String normalized = normalizeEnumValue(value);
    try {
      return Enum.valueOf(type, normalized);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException(
          "invalid value for " + key + ": " + value + "; expected one of " + enumValues(type),
          exception);
    }
  }

  private static String normalizeEnumValue(String value) {
    String trimmed = value.trim();
    StringBuilder builder = new StringBuilder(trimmed.length() + 4);
    char previous = 0;
    for (int index = 0; index < trimmed.length(); index++) {
      char character = trimmed.charAt(index);
      if (character == '-') {
        builder.append('_');
      } else {
        if (Character.isUpperCase(character)
            && index > 0
            && previous != '_'
            && previous != '-'
            && Character.isLowerCase(previous)) {
          builder.append('_');
        }
        builder.append(Character.toUpperCase(character));
      }
      previous = character;
    }
    return builder.toString();
  }

  private static boolean parseBoolean(String key, String value) {
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    if ("true".equals(normalized)) {
      return true;
    }
    if ("false".equals(normalized)) {
      return false;
    }
    throw new IllegalArgumentException(
        "invalid value for " + key + ": " + value + "; expected true or false");
  }

  private static <T extends Enum<T>> String enumValues(Class<T> type) {
    StringBuilder builder = new StringBuilder();
    T[] constants = type.getEnumConstants();
    for (int index = 0; index < constants.length; index++) {
      if (index > 0) {
        builder.append(", ");
      }
      builder.append(constants[index].name().toLowerCase(Locale.ROOT));
    }
    return builder.toString();
  }

  private static String firstNonBlank(String first, String second) {
    if (first != null && !first.isBlank()) {
      return first;
    }
    return second == null || second.isBlank() ? null : second;
  }

  /** Mutable builder for {@link MundaneLoggerConfig}. */
  public static final class Builder {
    private LogLevel rootLevel = LogLevel.INFO;
    private final Map<String, LogLevel> loggerLevels = new LinkedHashMap<>();
    private OutputFormat format = OutputFormat.JSONL;
    private SinkType sinkType = SinkType.STDERR;
    private Path filePath;
    private FileMode fileMode = FileMode.APPEND;
    private boolean includeThread = true;
    private ThrowableMode throwableMode = ThrowableMode.SUMMARY;
    private TimestampMode timestampMode = TimestampMode.ISO_INSTANT;

    public Builder rootLevel(LogLevel rootLevel) {
      this.rootLevel = Objects.requireNonNull(rootLevel, "rootLevel");
      return this;
    }

    public Builder level(String loggerName, LogLevel level) {
      if (loggerName == null || loggerName.isBlank()) {
        throw new IllegalArgumentException("logger name must not be blank");
      }
      loggerLevels.put(loggerName, Objects.requireNonNull(level, "level"));
      return this;
    }

    public Builder format(OutputFormat format) {
      this.format = Objects.requireNonNull(format, "format");
      return this;
    }

    public Builder sinkType(SinkType sinkType) {
      this.sinkType = Objects.requireNonNull(sinkType, "sinkType");
      return this;
    }

    public Builder filePath(Path filePath) {
      this.filePath = Objects.requireNonNull(filePath, "filePath");
      return this;
    }

    public Builder fileMode(FileMode fileMode) {
      this.fileMode = Objects.requireNonNull(fileMode, "fileMode");
      return this;
    }

    public Builder includeThread(boolean includeThread) {
      this.includeThread = includeThread;
      return this;
    }

    public Builder throwableMode(ThrowableMode throwableMode) {
      this.throwableMode = Objects.requireNonNull(throwableMode, "throwableMode");
      return this;
    }

    public Builder timestampMode(TimestampMode timestampMode) {
      this.timestampMode = Objects.requireNonNull(timestampMode, "timestampMode");
      return this;
    }

    public MundaneLoggerConfig build() {
      return new MundaneLoggerConfig(this);
    }
  }
}
