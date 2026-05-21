package io.github.mundanej.mlog.slf4j;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.core.MundaneLogManager;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.testkit.CapturingLogSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

final class Slf4jProviderTest {
  @AfterEach
  void resetLogger() {
    MDC.clear();
    MundaneLogManager.reset();
  }

  @Test
  void providerRoutesParameterizedMessagesThrowableMarkersAndMdc() {
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLogManager.install(MundaneLoggerConfig.defaults(), sink);
    org.slf4j.Logger logger = LoggerFactory.getLogger("slf4j.example");
    MDC.put("tenant", "tenant-1");

    logger.info(
        MarkerFactory.getMarker("AUDIT"),
        "created {}",
        "user-1",
        new IllegalStateException("boom"));

    String line = sink.singleLine();
    assertTrue(line.contains("\"logger\":\"slf4j.example\""));
    assertTrue(line.contains("\"message\":\"created user-1\""));
    assertTrue(line.contains("\"slf4j.marker\":\"AUDIT\""));
    assertTrue(line.contains("\"mdc.tenant\":\"tenant-1\""));
    assertTrue(line.contains("IllegalStateException"));
  }

  @Test
  void fluentKeyValuesAreStructuredFields() {
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLoggerConfig config = MundaneLoggerConfig.builder().rootLevel(LogLevel.DEBUG).build();
    MundaneLogManager.install(config, sink);
    org.slf4j.Logger logger = LoggerFactory.getLogger("slf4j.fluent");

    logger.atDebug().addKeyValue("userId", 7).setMessage("created").log();

    String line = sink.singleLine();
    assertTrue(line.contains("\"message\":\"created\""));
    assertTrue(line.contains("\"kv.userId\":\"7\""));
  }

  @Test
  void providerInitializationDoesNotReplaceExplicitBackend() {
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLogManager.install(MundaneLoggerConfig.defaults(), sink);

    new MundaneSlf4jServiceProvider().initialize();
    org.slf4j.Logger logger = LoggerFactory.getLogger("slf4j.explicit");
    logger.info("kept");

    assertTrue(sink.singleLine().contains("\"message\":\"kept\""));
  }
}
