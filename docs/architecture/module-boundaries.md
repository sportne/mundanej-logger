# Module Boundaries

- `api` contains public logging API types and the explicit backend registry.
- `core` contains configuration, level resolution, renderers, and synchronous
  stdout, stderr, and file sinks.
- `slf4j` contains the SLF4J 2.x provider and may use SLF4J's provider contract.
- `testkit` contains test-only helpers.
- `architecture-tests` enforces repository-wide forbidden API rules.
- `native-tests` contains Native Image smoke tests.
- `examples` contains non-published consumer quickstarts that exercise the
  documented dependency paths.

The `api` and `core` modules have no third-party runtime dependencies.
