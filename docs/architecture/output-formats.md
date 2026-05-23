# Output Formats

`mundane-logger` 1.0.0 supports two output formats: JSON Lines for structured
production logging and plain text for simple human-readable logs. Both formats
render one log event as one physical line followed by `\n`.

The runtime does not support format plugins, pattern languages, expression
evaluation, message lookups, schema negotiation, arrays, or nested structured
objects in 1.0.0.

## JSON Lines

JSON Lines is the default format. Each event is one valid JSON object followed
by `\n`. Top-level fields are emitted in this deterministic order:

1. `timestamp`
2. `level`
3. `logger`
4. `thread`
5. `event`
6. `message`
7. `throwable`
8. `context`
9. `fields`

`timestamp=isoInstant` renders the timestamp as an ISO-8601 `Instant` string.
`timestamp=epochMillis` renders the timestamp as a JSON number containing epoch
milliseconds.

`thread`, `event`, `message`, and `throwable` render as JSON `null` when not
present. `context` and `fields` always render as JSON objects. Entries inside
those objects preserve insertion order.

Structured scalar values render as follows:

| Value kind | JSON rendering |
|---|---|
| `String` | JSON string with standard escaping |
| `boolean` | JSON boolean |
| `int` / `long` | JSON number |
| finite `double` | JSON number |
| `NaN` / infinities | JSON string, such as `"NaN"` or `"Infinity"` |
| `BigDecimal` | JSON number using `toPlainString()` |
| `UUID` | JSON string |
| `Instant` | JSON string using `Instant.toString()` |
| null | JSON `null` |

Throwable summary mode renders `type`, `message`, `suppressed`, and `cause`.
Stacktrace mode adds `stackTrace`. Disabled throwable mode renders `null`.

User fields never overwrite reserved top-level fields because user data is kept
under `fields`. Context data is kept under `context`.

## Plain Text

Plain text output is intended for local and human-readable logs. It is not a
stable machine-parsing format.

Each event is one physical line followed by `\n`. Tokens are emitted in this
deterministic order:

1. timestamp
2. `level=...`
3. `logger=...`
4. `thread=...` when present
5. `event=...` when present
6. `message=...` when present
7. `context.<key>=...` entries in insertion order
8. `field.<key>=...` entries in insertion order
9. `throwable=...` when a throwable was supplied

Text values escape newline, carriage return, tab, backslash, and other control
characters so logged data cannot forge additional physical log lines. Spaces
and `=` characters are not escaped, so consumers should prefer JSON Lines when
machine parsing is required.

Throwable summary mode renders the type, message, suppressed summaries, and
cause summaries inline. Stacktrace mode appends an escaped stack trace to the
same physical line. Disabled throwable mode renders `throwable=null` when a
throwable was supplied.
