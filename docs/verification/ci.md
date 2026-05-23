# CI Verification

GitHub Actions mirrors the local release-readiness lanes used before a version
tag is created.

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

The `github-release` workflow runs on tags matching `v*`. It stages the
Maven-layout artifacts with the tag version and attaches those artifacts to a
GitHub Release.

The push and pull-request verification workflows use read-only repository
permissions. The tag release workflow uses `contents: write` only to create the
GitHub Release and upload release assets. No workflow configures signing keys,
remote Maven publishing repositories, Maven Central credentials, or branch
protection.
