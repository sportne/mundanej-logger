package io.github.mundanej.mlog.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.core.config.FileMode;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.config.OutputFormat;
import io.github.mundanej.mlog.core.config.SinkType;
import io.github.mundanej.mlog.core.config.ThrowableMode;
import io.github.mundanej.mlog.core.config.TimestampMode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ConfigurationTest {
  @TempDir Path tempDirectory;

  @Test
  void loggerLevelUsesLongestPackagePrefix() {
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder()
            .rootLevel(LogLevel.ERROR)
            .level("com.example", LogLevel.WARN)
            .level("com.example.deep", LogLevel.DEBUG)
            .build();

    assertEquals(LogLevel.DEBUG, config.levelFor("com.example.deep.Service"));
    assertEquals(LogLevel.WARN, config.levelFor("com.example.Other"));
    assertEquals(LogLevel.ERROR, config.levelFor("org.example.Other"));
  }

  @Test
  void defaultsAreProductionJsonToStderr() {
    MundaneLoggerConfig config = MundaneLoggerConfig.defaults();

    assertEquals(LogLevel.INFO, config.rootLevel());
    assertEquals(OutputFormat.JSONL, config.format());
  }

  @Test
  void loadsPropertiesFileFromSystemProperty() throws IOException {
    Path logFile = tempDirectory.resolve("app.log");
    Path configFile = tempDirectory.resolve("logger.properties");
    Files.writeString(
        configFile,
        String.join(
            "\n",
            "mundane.logger.rootLevel=warn",
            "mundane.logger.format=text",
            "mundane.logger.sink=file",
            "mundane.logger.file.path=" + logFile,
            "mundane.logger.file.mode=truncate",
            "mundane.logger.includeThread=false",
            "mundane.logger.throwable=stacktrace",
            "mundane.logger.timestamp=epochMillis",
            "mundane.logger.level.com.example=debug",
            ""));
    Properties systemProperties = new Properties();
    systemProperties.setProperty("mundane.logger.configFile", configFile.toString());

    MundaneLoggerConfig config = MundaneLoggerConfig.load(systemProperties, Map.of());

    assertEquals(LogLevel.WARN, config.rootLevel());
    assertEquals(OutputFormat.TEXT, config.format());
    assertEquals(SinkType.FILE, config.sinkType());
    assertEquals(logFile, config.filePath());
    assertEquals(FileMode.TRUNCATE, config.fileMode());
    assertEquals(false, config.includeThread());
    assertEquals(ThrowableMode.STACKTRACE, config.throwableMode());
    assertEquals(TimestampMode.EPOCH_MILLIS, config.timestampMode());
    assertEquals(LogLevel.DEBUG, config.levelFor("com.example.Service"));
  }

  @Test
  void systemPropertiesOverrideEnvironmentAndConfigFile() throws IOException {
    Path configFile = tempDirectory.resolve("logger.properties");
    Files.writeString(configFile, "mundane.logger.rootLevel=trace\n");
    Properties systemProperties = new Properties();
    systemProperties.setProperty("mundane.logger.configFile", configFile.toString());
    systemProperties.setProperty("mundane.logger.rootLevel", "error");
    Map<String, String> environment =
        Map.of("MUNDANE_LOGGER_ROOT_LEVEL", "warn", "MUNDANE_LOGGER_CONFIG_FILE", "ignored");

    MundaneLoggerConfig config = MundaneLoggerConfig.load(systemProperties, environment);

    assertEquals(LogLevel.ERROR, config.rootLevel());
  }

  @Test
  void environmentOverridesPropertiesFile() throws IOException {
    Path configFile = tempDirectory.resolve("logger.properties");
    Files.writeString(configFile, "mundane.logger.rootLevel=trace\n");
    Properties systemProperties = new Properties();
    systemProperties.setProperty("mundane.logger.configFile", configFile.toString());

    MundaneLoggerConfig config =
        MundaneLoggerConfig.load(systemProperties, Map.of("MUNDANE_LOGGER_ROOT_LEVEL", "debug"));

    assertEquals(LogLevel.DEBUG, config.rootLevel());
  }

  @Test
  void environmentLoggerOverrideMapsUnderscoresToLowercasePackageName() {
    MundaneLoggerConfig config =
        MundaneLoggerConfig.load(
            new Properties(), Map.of("MUNDANE_LOGGER_LEVEL_COM_EXAMPLE_SERVICE", "trace"));

    assertEquals(LogLevel.TRACE, config.levelFor("com.example.service.UserService"));
  }

  @Test
  void invalidEnumValuesNameTheBadKeyAndValue() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("mundane.logger.rootLevel", "verbose");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> MundaneLoggerConfig.load(systemProperties, Map.of()));

    assertTrue(exception.getMessage().contains("mundane.logger.rootLevel"));
    assertTrue(exception.getMessage().contains("verbose"));
    assertTrue(exception.getMessage().contains("trace"));
  }

  @Test
  void invalidBooleanValuesAreRejected() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("mundane.logger.includeThread", "maybe");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> MundaneLoggerConfig.load(systemProperties, Map.of()));

    assertTrue(exception.getMessage().contains("mundane.logger.includeThread"));
    assertTrue(exception.getMessage().contains("maybe"));
    assertTrue(exception.getMessage().contains("true or false"));
  }

  @Test
  void fileSinkRequiresFilePath() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("mundane.logger.sink", "file");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> MundaneLoggerConfig.load(systemProperties, Map.of()));

    assertEquals("file.path is required when sink=file", exception.getMessage());
  }

  @Test
  void blankSystemPropertyLoggerOverrideNameIsRejected() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("mundane.logger.level.", "debug");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> MundaneLoggerConfig.load(systemProperties, Map.of()));

    assertEquals("logger name must not be blank for mundane.logger.level.", exception.getMessage());
  }

  @Test
  void blankEnvironmentLoggerOverrideNameIsRejected() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                MundaneLoggerConfig.load(
                    new Properties(), Map.of("MUNDANE_LOGGER_LEVEL_", "debug")));

    assertEquals("logger name must not be blank for MUNDANE_LOGGER_LEVEL_", exception.getMessage());
  }
}
