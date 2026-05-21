package io.github.mundanej.mlog.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.config.OutputFormat;
import org.junit.jupiter.api.Test;

final class ConfigurationTest {
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
}
