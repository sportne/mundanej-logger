# Architecture Gates

Architecture tests prevent the core runtime from depending on reflection,
method handles, JNDI, scripting, networking APIs, process spawning, Java object
serialization, service loading, native methods, and finalizers.

SLF4J provider discovery is isolated to the `slf4j` module's service descriptor.
