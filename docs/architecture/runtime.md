# Runtime Architecture

Applications install a core backend explicitly through `MundaneLogManager`.
`MundaneLogger` instances are lightweight name holders that consult the current
backend at call time, so static logger fields remain usable after configuration.

The v1 runtime is synchronous. A renderer turns one event into one physical line
and a sink writes that line to stdout, stderr, or a file.
