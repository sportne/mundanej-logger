# Charter

`mundane-logger` provides a tiny, deterministic logging API and backend for Java
applications that need structured output without heavyweight runtime features.

The project is secure by construction: logged data is inert data and cannot
trigger class loading, lookup evaluation, scripting, JNDI, network access, or
reflection-based behavior.
