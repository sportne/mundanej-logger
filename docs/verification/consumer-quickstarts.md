# Consumer Quickstart Verification

Consumer quickstarts are checked source examples for the public dependency
paths documented in the README.

Run all quickstart checks through the default gate:

```bash
./gradlew check --console=plain
```

Run an individual quickstart when changing one dependency path:

```bash
./gradlew :examples:native-api-quickstart:check --console=plain
./gradlew :examples:slf4j-provider-quickstart:check --console=plain
```

The quickstarts use project dependencies inside this repository, but mirror the
published artifact relationships documented for consumers:

- Native API bootstrap compiles against `mundane-logger-core`, which exposes the
  native API types and contains `MundaneLogManager`.
- SLF4J provider usage compiles against `mundane-logger-slf4j`, which exposes
  SLF4J API types and routes calls into the installed mundane backend.

The example projects are not published artifacts. Release dry-run validation
must continue to stage only the BOM, API, core, SLF4J provider, and testkit.
