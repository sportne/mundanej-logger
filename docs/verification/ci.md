# CI Verification

GitHub Actions mirrors the local release-readiness lanes used before v0.1
publication.

The `ci` workflow runs on pushes and pull requests to `main`.

- `check` runs `./gradlew check --console=plain` on Temurin Java 21 and 25.
- `release-dry-run` runs
  `./gradlew releaseDryRun printPublishedArtifacts --console=plain` on Temurin
  Java 21.

The `native-image` workflow runs on pull requests to `main` and manual dispatch.
It uses GraalVM Java 21 with Native Image available and runs:

```bash
./gradlew nativeSmoke --console=plain
```

The workflows use read-only repository permissions. They do not configure
signing keys, remote publishing repositories, Maven Central credentials, release
tags, or branch protection.
