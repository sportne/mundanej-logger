# mundane-logger

`mundane-logger` is a small Java 21 logging runtime focused on structured logs,
deterministic behavior, and GraalVM Native Image friendliness.

The core runtime intentionally avoids expression languages, message lookups,
JNDI, remote appenders, dynamic plugins, reflection-based configuration, and
runtime scanning.

## Native API

```java
MundaneLogManager.installDefault();

private static final MundaneLogger LOG = MundaneLogger.get("com.example.UserService");

LOG.info()
    .event("user.created")
    .field("userId", userId)
    .message("User created")
    .emit();
```

## Modules

- `mundane-logger-api`
- `mundane-logger-core`
- `mundane-logger-slf4j`
- `mundane-logger-testkit`
- `mundane-logger-bom`

Configuration details are documented in
[`docs/configuration.md`](docs/configuration.md).

Output format details are documented in
[`docs/architecture/output-formats.md`](docs/architecture/output-formats.md).
