package io.github.mundanej.mlog.examples.nativeapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.core.MundaneLogManager;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.testkit.CapturingLogSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class NativeApiQuickstartTest {
  @AfterEach
  void resetLogger() {
    MundaneLogManager.reset();
  }

  @Test
  void nativeApiQuickstartEmitsJsonLine() {
    CapturingLogSink sink = new CapturingLogSink();
    MundaneLogManager.install(MundaneLoggerConfig.defaults(), sink);

    NativeApiQuickstart.emitUserCreated("user-1");

    String line = sink.singleLine();
    assertTrue(
        line.contains(
            "\"logger\":\"io.github.mundanej.mlog.examples.nativeapi.NativeApiQuickstart\""));
    assertTrue(line.contains("\"event\":\"user.created\""));
    assertTrue(line.contains("\"message\":\"User created\""));
    assertTrue(line.contains("\"userId\":\"user-1\""));
  }
}
