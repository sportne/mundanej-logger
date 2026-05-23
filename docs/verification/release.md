# Release Verification

The release dry-run lane verifies local publication readiness without signing,
uploading to Maven Central, or publishing to any remote repository.

## Commands

Run the default JVM gate first:

```bash
./gradlew check --console=plain
```

Print the planned artifact coordinates:

```bash
./gradlew printPublishedArtifacts --console=plain
```

Run the local release dry-run:

```bash
./gradlew releaseDryRun --console=plain
```

The compatibility alias remains available:

```bash
./gradlew publicationDryRun --console=plain
```

The dry-run publishes public artifacts to:

```text
build/release-dry-run/maven
```

## Published Artifacts

| Project | Coordinate |
|---|---|
| `:modules:mundane-logger-bom` | `io.github.mundanej:mundane-logger-bom` |
| `:modules:api` | `io.github.mundanej:mundane-logger-api` |
| `:modules:core` | `io.github.mundanej:mundane-logger-core` |
| `:modules:slf4j` | `io.github.mundanej:mundane-logger-slf4j` |
| `:modules:testkit` | `io.github.mundanej:mundane-logger-testkit` |

`releaseDryRun` verifies that each published POM has project description,
BSD 3-Clause license metadata, developer metadata, and SCM metadata. It also
checks that Java artifacts include binary, sources, and Javadoc jars, and that
the BOM aligns every public non-BOM module.

Published POM URL and SCM metadata must match the configured GitHub repository:
`https://github.com/sportne/mundanej-logger`.

## Version Override

The default local version is `0.1.0-SNAPSHOT`. To test another version without
editing repository files, pass the Gradle property:

```bash
./gradlew releaseDryRun -Pmlog.version=0.1.0 --console=plain
```

## Non-Goals

- No signing keys are configured.
- No Maven Central or Sonatype upload is performed.
- No remote publishing repository is configured.
- No generated dry-run output under `build/` is intended to be committed.
