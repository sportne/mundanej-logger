package io.github.mundanej.mlog.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlog.api.LogContext;
import io.github.mundanej.mlog.api.LogValue;
import io.github.mundanej.mlog.api.MundaneLogger;
import io.github.mundanej.mlog.core.config.MundaneLoggerConfig;
import io.github.mundanej.mlog.core.config.OutputFormat;
import io.github.mundanej.mlog.core.config.ThrowableMode;
import io.github.mundanej.mlog.core.config.TimestampMode;
import io.github.mundanej.mlog.core.sink.LogSink;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class OutputFormatContractTest {
  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-05-20T12:34:56Z"), ZoneOffset.UTC);

  @AfterEach
  void resetLogger() {
    LogContext.clear();
    MundaneLogManager.reset();
  }

  @Test
  void jsonLinesRendersScalarValuesInDeterministicOrder() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config = MundaneLoggerConfig.builder().includeThread(false).build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);

    try (LogContext.Scope tenant = LogContext.put("tenant", LogValue.of("tenant-1"));
        LogContext.Scope region = LogContext.put("region", LogValue.of("us-east-1"))) {
      assertTrue(tenant != null);
      assertTrue(region != null);
      MundaneLogger.get("scalar.logger")
          .info()
          .event("scalar.event")
          .message("hello")
          .field("stringValue", "quote \" slash \\ newline \n carriage \r tab \t control \u0001")
          .field("booleanValue", true)
          .field("intValue", 7)
          .field("longValue", 9007199254740993L)
          .field("doubleValue", 12.5d)
          .field("nanValue", Double.NaN)
          .field("infiniteValue", Double.POSITIVE_INFINITY)
          .field("decimalValue", new BigDecimal("123.4500"))
          .field("uuidValue", UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
          .field("instantValue", Instant.parse("2026-05-20T12:35:00Z"))
          .fieldNull("nullValue")
          .emit();
    }

    assertEquals(
        "{\"timestamp\":\"2026-05-20T12:34:56Z\",\"level\":\"INFO\","
            + "\"logger\":\"scalar.logger\",\"thread\":null,\"event\":\"scalar.event\","
            + "\"message\":\"hello\",\"throwable\":null,"
            + "\"context\":{\"tenant\":\"tenant-1\",\"region\":\"us-east-1\"},"
            + "\"fields\":{\"stringValue\":\"quote \\\" slash \\\\ newline \\n carriage \\r tab"
            + " \\t control \\u0001\",\"booleanValue\":true,\"intValue\":7,"
            + "\"longValue\":9007199254740993,\"doubleValue\":12.5,\"nanValue\":\"NaN\","
            + "\"infiniteValue\":\"Infinity\",\"decimalValue\":123.4500,"
            + "\"uuidValue\":\"123e4567-e89b-12d3-a456-426614174000\","
            + "\"instantValue\":\"2026-05-20T12:35:00Z\",\"nullValue\":null}}\n",
        sink.singleLine());
  }

  @Test
  void jsonLinesUsesEpochMillisAndEmptyObjectsWhenOptionalValuesAreMissing() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder()
            .includeThread(false)
            .timestampMode(TimestampMode.EPOCH_MILLIS)
            .build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);

    MundaneLogger.get("empty.logger").error().emit();

    assertEquals(
        "{\"timestamp\":1779280496000,\"level\":\"ERROR\",\"logger\":\"empty.logger\","
            + "\"thread\":null,\"event\":null,\"message\":null,\"throwable\":null,"
            + "\"context\":{},\"fields\":{}}\n",
        sink.singleLine());
  }

  @Test
  void jsonLinesRendersThrowableSummaryCauseSuppressedAndDisabledModes() {
    CapturingSink summarySink = new CapturingSink();
    MundaneLogManager.install(
        MundaneLoggerConfig.builder()
            .includeThread(false)
            .throwableMode(ThrowableMode.SUMMARY)
            .build(),
        summarySink,
        FIXED_CLOCK);

    MundaneLogger.get("throwable.summary")
        .error()
        .message("boom")
        .throwable(failureWithCauseAndSuppressed())
        .emit();

    assertEquals(
        "{\"timestamp\":\"2026-05-20T12:34:56Z\",\"level\":\"ERROR\","
            + "\"logger\":\"throwable.summary\",\"thread\":null,\"event\":null,"
            + "\"message\":\"boom\",\"throwable\":{\"type\":\"java.lang.IllegalStateException\","
            + "\"message\":\"failed\",\"suppressed\":[{\"type\":\"java.lang.IllegalArgumentException\","
            + "\"message\":\"bad\",\"suppressed\":[],\"cause\":null}],"
            + "\"cause\":{\"type\":\"java.lang.RuntimeException\",\"message\":\"root\","
            + "\"suppressed\":[],\"cause\":null}},\"context\":{},\"fields\":{}}\n",
        summarySink.singleLine());

    CapturingSink disabledSink = new CapturingSink();
    MundaneLogManager.install(
        MundaneLoggerConfig.builder()
            .includeThread(false)
            .throwableMode(ThrowableMode.DISABLED)
            .build(),
        disabledSink,
        FIXED_CLOCK);

    MundaneLogger.get("throwable.disabled")
        .error()
        .message("boom")
        .throwable(new IllegalStateException("hidden"))
        .emit();

    assertEquals(
        "{\"timestamp\":\"2026-05-20T12:34:56Z\",\"level\":\"ERROR\","
            + "\"logger\":\"throwable.disabled\",\"thread\":null,\"event\":null,"
            + "\"message\":\"boom\",\"throwable\":null,\"context\":{},\"fields\":{}}\n",
        disabledSink.singleLine());
  }

  @Test
  void jsonLinesStackTraceModeKeepsStackTraceInsideSingleJsonLine() {
    CapturingSink sink = new CapturingSink();
    MundaneLogManager.install(
        MundaneLoggerConfig.builder()
            .includeThread(false)
            .throwableMode(ThrowableMode.STACKTRACE)
            .build(),
        sink,
        FIXED_CLOCK);

    MundaneLogger.get("throwable.stacktrace")
        .error()
        .throwable(new IllegalStateException("failed"))
        .emit();

    String line = sink.singleLine();
    assertOnePhysicalLine(line);
    assertTrue(line.contains("\"stackTrace\":\"java.lang.IllegalStateException: failed\\n"));
  }

  @Test
  void plainTextEscapesValuesAndThrowableSoEventsRemainOnePhysicalLine() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder()
            .format(OutputFormat.TEXT)
            .includeThread(false)
            .throwableMode(ThrowableMode.STACKTRACE)
            .build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);

    try (LogContext.Scope tenant = LogContext.put("tenant", LogValue.of("a\nb"))) {
      assertTrue(tenant != null);
      MundaneLogger.get("plain\nlogger")
          .warn()
          .event("event\rname")
          .message("first\r\nsecond\tthird\u0001")
          .field("value", "x\\y")
          .throwable(new IllegalStateException("bad\nvalue"))
          .emit();
    }

    String line = sink.singleLine();
    assertOnePhysicalLine(line);
    assertTrue(
        line.startsWith(
            "2026-05-20T12:34:56Z level=WARN logger=plain\\nlogger "
                + "event=event\\rname message=first\\r\\nsecond\\tthird\\u0001 "
                + "context.tenant=a\\nb field.value=x\\\\y "
                + "throwable=java.lang.IllegalStateException:bad\\nvalue "
                + "stackTrace=java.lang.IllegalStateException: bad\\nvalue\\n"));
  }

  @Test
  void plainTextOmitsOptionalTokensAndSupportsEpochMillis() {
    CapturingSink sink = new CapturingSink();
    MundaneLoggerConfig config =
        MundaneLoggerConfig.builder()
            .format(OutputFormat.TEXT)
            .includeThread(false)
            .timestampMode(TimestampMode.EPOCH_MILLIS)
            .build();
    MundaneLogManager.install(config, sink, FIXED_CLOCK);

    MundaneLogger.get("plain.empty").info().emit();

    assertEquals("1779280496000 level=INFO logger=plain.empty\n", sink.singleLine());
  }

  private static IllegalStateException failureWithCauseAndSuppressed() {
    IllegalStateException failure = new IllegalStateException("failed");
    failure.addSuppressed(new IllegalArgumentException("bad"));
    failure.initCause(new RuntimeException("root"));
    return failure;
  }

  private static void assertOnePhysicalLine(String line) {
    assertTrue(line.endsWith("\n"));
    assertFalse(line.substring(0, line.length() - 1).contains("\n"));
    assertFalse(line.contains("\r"));
  }

  private static final class CapturingSink implements LogSink {
    private final List<String> lines = new ArrayList<>();

    @Override
    public void write(byte[] line) {
      lines.add(new String(line, StandardCharsets.UTF_8));
    }

    @Override
    public void close() {}

    String singleLine() {
      assertEquals(1, lines.size());
      return lines.get(0);
    }
  }
}
