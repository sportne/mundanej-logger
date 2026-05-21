# Configuration

`mundane-logger` configuration is deliberately small and deterministic. The
core runtime reads local process configuration once during
`MundaneLogManager.installDefault()`. It does not reload configuration, expand
variables, load remote files, deserialize JSON/YAML/XML, or instantiate classes
named in configuration.

## Precedence

Configuration values are applied in this order, from lowest to highest
precedence:

1. Built-in defaults.
2. Optional Java properties file.
3. Environment variables.
4. System properties.
5. Programmatic `MundaneLoggerConfig` supplied to `MundaneLogManager.install`.

The optional properties file is selected with either:

- system property `mundane.logger.configFile`
- environment variable `MUNDANE_LOGGER_CONFIG_FILE`

If both are present, the system property selects the file.

## Defaults

| Setting | Default |
|---|---|
| Root level | `INFO` |
| Format | `JSONL` |
| Sink | `STDERR` |
| File mode | `APPEND` |
| Include thread | `true` |
| Throwable rendering | `SUMMARY` |
| Timestamp | `ISO_INSTANT` |

## Supported Values

Levels:

- `TRACE`
- `DEBUG`
- `INFO`
- `WARN`
- `ERROR`
- `OFF`

Formats:

- `JSONL`
- `TEXT`

Sinks:

- `STDOUT`
- `STDERR`
- `FILE`

File modes:

- `APPEND`
- `TRUNCATE`

Throwable modes:

- `DISABLED`
- `SUMMARY`
- `STACKTRACE`

Timestamp modes:

- `ISO_INSTANT`
- `EPOCH_MILLIS`

Enum values are case-insensitive. Hyphenated and camel-case values are accepted
where an enum uses an underscore, such as `epoch-millis` or `epochMillis`.

Boolean values must be exactly `true` or `false`, ignoring case and surrounding
whitespace. Other values are rejected.

## Keys

| Meaning | System property / properties file | Environment variable |
|---|---|---|
| Root level | `mundane.logger.rootLevel` | `MUNDANE_LOGGER_ROOT_LEVEL` |
| Format | `mundane.logger.format` | `MUNDANE_LOGGER_FORMAT` |
| Sink | `mundane.logger.sink` | `MUNDANE_LOGGER_SINK` |
| File path | `mundane.logger.file.path` | `MUNDANE_LOGGER_FILE_PATH` |
| File mode | `mundane.logger.file.mode` | `MUNDANE_LOGGER_FILE_MODE` |
| Include thread | `mundane.logger.includeThread` | `MUNDANE_LOGGER_INCLUDE_THREAD` |
| Throwable mode | `mundane.logger.throwable` | `MUNDANE_LOGGER_THROWABLE` |
| Timestamp mode | `mundane.logger.timestamp` | `MUNDANE_LOGGER_TIMESTAMP` |

Per-logger system property and properties-file overrides use:

```properties
mundane.logger.level.com.example=DEBUG
```

Per-logger environment overrides use:

```text
MUNDANE_LOGGER_LEVEL_COM_EXAMPLE=DEBUG
```

The environment suffix is lowercased and `_` is mapped to `.`, so
`MUNDANE_LOGGER_LEVEL_COM_EXAMPLE_SERVICE=TRACE` configures
`com.example.service`.

## File Sink

When `sink=FILE`, `file.path` / `MUNDANE_LOGGER_FILE_PATH` is required. Parent
directories are created if needed. `APPEND` preserves existing content, while
`TRUNCATE` replaces existing content when the sink opens.

## Examples

System properties:

```shell
java \
  -Dmundane.logger.rootLevel=INFO \
  -Dmundane.logger.format=jsonl \
  -Dmundane.logger.sink=stderr \
  -Dmundane.logger.level.com.example=DEBUG \
  -jar app.jar
```

Environment variables:

```shell
MUNDANE_LOGGER_ROOT_LEVEL=INFO \
MUNDANE_LOGGER_FORMAT=JSONL \
MUNDANE_LOGGER_SINK=FILE \
MUNDANE_LOGGER_FILE_PATH=/var/log/app/events.jsonl \
MUNDANE_LOGGER_LEVEL_COM_EXAMPLE=DEBUG \
java -jar app.jar
```

Properties file:

```properties
mundane.logger.rootLevel=INFO
mundane.logger.format=jsonl
mundane.logger.sink=file
mundane.logger.file.path=/var/log/app/events.jsonl
mundane.logger.file.mode=append
mundane.logger.includeThread=true
mundane.logger.throwable=summary
mundane.logger.timestamp=isoInstant
mundane.logger.level.com.example=DEBUG
```
