package io.github.mundanej.mlog.examples.slf4j;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.core.MundaneLogManager;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.testkit.CapturingLogSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class Slf4jProviderQuickstartTest {
  @AfterEach
  void resetLogger() {
    MundaneLogManager.reset();
  }

  @Test
  void slf4jProviderQuickstartEmitsJsonLine() {
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLogManager.install(MundaneLoggerConfig.defaults(), sink);

    Slf4jProviderQuickstart.emitUserCreated("user-1");

    String line = sink.singleLine();
    assertTrue(
        line.contains(
            "\"logger\":\"io.github.mundanej.mlog.examples.slf4j.Slf4jProviderQuickstart\""));
    assertTrue(line.contains("\"message\":\"User created user-1\""));
  }
}
