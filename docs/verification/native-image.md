# Native Image

The core API and runtime are designed to require no reflection configuration.

Native smoke tests exercise the native API, JSON Lines rendering, core
configuration, and the SLF4J module on the JVM discovery pass used by GraalVM
Native Build Tools.

The local JVM verification gate runs with:

```shell
./gradlew check --console=plain
```

The native binary smoke lane requires a GraalVM JDK with `native-image`
available. With SDKMAN-managed GraalVM, run:

```shell
source "$HOME/.sdkman/bin/sdkman-init.sh" && ./gradlew nativeSmoke --console=plain
```
