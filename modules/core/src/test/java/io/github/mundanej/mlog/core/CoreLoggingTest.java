package io.github.mundanej.mlog.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.api.LogContext;
import io.github.mundanej.mlog.api.LogLevel;
import io.github.mundanej.mlog.api.LogValue;
import io.github.mundanej.mlog.api.MundaneLogger;
import io.github.mundanej.mlog.core.config.FileMode;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.config.OutputFormat;
import io.github.mundanej.mlog.core.config.SinkType;
import io.github.mundanej.mlog.core.config.ThrowableMode;
import io.github.mundanej.mlog.core.sink.LogSink;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class CoreLoggingTest {
  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-05-20T12:34:56Z"), ZoneOffset.UTC);

  @TempDir java.nio.file.Path tempDirectory;

  @AfterEach
  void resetLogger() {
    LogContext.clear();
    MundaneLogManager.reset();
  }

  @Test
  void writesDeterministicJsonLineWithContextAndFields() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config = MundaneLoggerConfig.builder().includeThread(false).build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);
    MundaneLogger logger = MundaneLogger.get("com.example.UserService");

    try (LogContext.Scope scope = LogContext.put("tenantId", LogValue.of("tenant-1"))) {
      assertTrue(scope != null);
      logger.info().event("user.created").message("Created\nuser").field("userId", 42L).emit();
    }

    assertEquals(
        "{\"timestamp\":\"2026-05-20T12:34:56Z\",\"level\":\"INFO\",\"logger\":\"com.example.UserService\","
            + "\"thread\":null,\"event\":\"user.created\",\"message\":\"Created\\nuser\","
            + "\"throwable\":null,\"context\":{\"tenantId\":\"tenant-1\"},\"fields\":{\"userId\":42}}\n",
        sink.singleLine());
  }

  @Test
  void writesOneLinePlainTextWithEscapedMessage() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder().format(OutputFormat.TEXT).includeThread(false).build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);

    MundaneLogger.get("example").warn().message("first\r\nsecond").field("ok", true).emit();

    assertEquals(
        "2026-05-20T12:34:56Z level=WARN logger=example message=first\\r\\nsecond field.ok=true\n",
        sink.singleLine());
  }

  @Test
  void honorsLevelOverridesAndDisabledLevelsAreNoOp() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder()
            .rootLevel(LogLevel.ERROR)
            .level("com.example", LogLevel.DEBUG)
            .build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);

    MundaneLogger root = MundaneLogger.get("other");
    MundaneLogger nested = MundaneLogger.get("com.example.Service");

    assertFalse(root.isInfoEnabled());
    root.info().field("bad name", "not validated when disabled").emit();
    assertTrue(nested.isDebugEnabled());
    nested.debug().message("debug").emit();

    assertEquals(1, sink.lines().size());
  }

  @Test
  void validatesFieldNamesWhenEnabled() {
    CapturingSink sink = new CapturingSink();
    MundaneLogManager.install(MundaneLoggerConfig.defaults(), sink, FIXED_CLOCK);

    assertThrows(
        IllegalArgumentException.class,
        () -> MundaneLogger.get("example").info().field("bad name", "value"));
  }

  @Test
  void writesFileSinkWithTruncateMode() throws IOException {
    java.nio.file.Path logFile = tempDirectory.resolve("app.log");
    Files.writeString(logFile, "old", StandardCharsets.UTF_8);
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder()
            .sinkType(SinkType.FILE)
            .filePath(logFile)
            .fileMode(FileMode.TRUNCATE)
            .build();
    MundaneLogManager.install(config);

    MundaneLogger.get("file").info().message("new").emit();
    MundaneLogManager.reset();

    String content = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(content.contains("\"message\":\"new\""));
    assertFalse(content.contains("old"));
  }

  @Test
  void rendersThrowableSummary() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder().throwableMode(ThrowableMode.SUMMARY).build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);

    IllegalStateException failure = new IllegalStateException("failed");
    MundaneLogger.get("example").error().message("boom").throwable(failure).emit();

    assertTrue(sink.singleLine().contains("\"type\":\"java.lang.IllegalStateException\""));
    assertTrue(sink.singleLine().contains("\"message\":\"failed\""));
  }

  private static final class CapturingSink implements LogSink {
    private final List<String> lines = new ArrayList<>();

    @Override
    public void write(byte[] line) {
      lines.add(new String(line, StandardCharsets.UTF_8));
    }

    @Override
    public void close() {}

    List<String> lines() {
      return List.copyOf(lines);
    }

    String singleLine() {
      assertEquals(1, lines.size());
      return lines.get(0);
    }
  }
}
