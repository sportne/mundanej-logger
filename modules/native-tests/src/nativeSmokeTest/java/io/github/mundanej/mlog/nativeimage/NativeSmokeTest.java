package io.github.mundanej.mlog.nativeimage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.api.MundaneLogger;
import io.github.mundanej.mlog.core.MundaneLogManager;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.testkit.CapturingLogSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
}
