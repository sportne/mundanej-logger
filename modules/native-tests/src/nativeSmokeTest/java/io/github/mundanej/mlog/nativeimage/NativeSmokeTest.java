package io.github.mundanej.mlog.nativeimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.api.MundaneLogger;
import io.github.mundanej.mlog.core.MundaneLogManager;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.testkit.CapturingLogSink;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

final class NativeSmokeTest {
  @AfterEach
  void resetLogger() {
    MundaneLogManager.reset();
  }

  @Test
  void logsThroughNativeApi() {
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLogManager.install(MundaneLoggerConfig.defaults(), sink);

    MundaneLogger.get("native").info().event("native.smoke").message("ok").emit();

    assertTrue(sink.singleLine().contains("\"event\":\"native.smoke\""));
  }

  @Test
  void appliesCoreConfiguration() {
    Properties properties = new Properties();
    properties.setProperty("mundane.logger.rootLevel", "warn");
    properties.setProperty("mundane.logger.includeThread", "false");
    MundaneLoggerConfig config = MundaneLoggerConfig.load(properties, Map.of());
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLogManager.install(config, sink);

    MundaneLogger logger = MundaneLogger.get("native.config");
    logger.info().message("hidden").emit();
    logger.warn().message("shown").emit();

    assertEquals(LogLevel.WARN, config.rootLevel());
    assertEquals(1, sink.lines().size());
    String line = sink.singleLine();
    assertTrue(line.contains("\"logger\":\"native.config\""));
    assertTrue(line.contains("\"thread\":null"));
    assertTrue(line.contains("\"message\":\"shown\""));
  }

  @Test
  void logsThroughSlf4jProvider() {
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLogManager.install(MundaneLoggerConfig.defaults(), sink);

    LoggerFactory.getLogger("native.slf4j").info("hello {}", "provider");

    String line = sink.singleLine();
    assertTrue(line.contains("\"logger\":\"native.slf4j\""));
    assertTrue(line.contains("\"message\":\"hello provider\""));
  }
}
