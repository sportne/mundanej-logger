# mundane-logger

`mundane-logger` is a small Java 21 logging runtime focused on structured logs,
deterministic behavior, and GraalVM Native Image friendliness.

The core runtime intentionally avoids expression languages, message lookups,
JNDI, remote appenders, dynamic plugins, reflection-based configuration, and
runtime scanning.

## Quickstart From Source

Build and verify the repository with the default local gate:

```bash
./gradlew check --console=plain
```

Native Image smoke tests are separate because they require a GraalVM Java 21
toolchain with `native-image`:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && ./gradlew nativeSmoke --console=plain
```

## Native API

Gradle consumers can align artifacts with the BOM:

```groovy
dependencies {
  implementation platform('io.github.mundanej:mundane-logger-bom:0.1.0-SNAPSHOT')
  implementation 'io.github.mundanej:mundane-logger-api'
  runtimeOnly 'io.github.mundanej:mundane-logger-core'
}
```

```java
MundaneLogManager.installDefault();

private static final MundaneLogger LOG = MundaneLogger.get("com.example.UserService");

LOG.info()
    .event("user.created")
    .field("userId", userId)
    .message("User created")
    .emit();
```

## SLF4J Provider

```groovy
dependencies {
  implementation platform('io.github.mundanej:mundane-logger-bom:0.1.0-SNAPSHOT')
  implementation 'io.github.mundanej:mundane-logger-slf4j'
}
```

## Artifacts

The planned v0.1 Maven coordinates are:

| Artifact | Coordinate |
|---|---|
| BOM | `io.github.mundanej:mundane-logger-bom` |
| Native API | `io.github.mundanej:mundane-logger-api` |
| Core runtime | `io.github.mundanej:mundane-logger-core` |
| SLF4J 2.x provider | `io.github.mundanej:mundane-logger-slf4j` |
| Test helpers | `io.github.mundanej:mundane-logger-testkit` |

Configuration details are documented in
[`docs/configuration.md`](docs/configuration.md).

Output format details are documented in
[`docs/architecture/output-formats.md`](docs/architecture/output-formats.md).

Native Image verification is documented in
[`docs/verification/native-image.md`](docs/verification/native-image.md).

Release dry-run verification is documented in
[`docs/verification/release.md`](docs/verification/release.md).
