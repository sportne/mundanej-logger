# ADR-0001: Secure Deterministic Logging Baseline

## Status

Accepted.

## Decision

The default runtime supports only deterministic local logging: JSON Lines or
plain text rendered to stdout, stderr, or a file.

The core runtime will not support JNDI, expression evaluation, message lookups,
remote appenders, scripting, dynamic plugins, dynamic class loading, annotation
scanning, reflection-based configuration, or network behavior.

## Consequences

Applications that need remote shipping or cloud/vendor integrations must use a
separate opt-in module or external log collector.
