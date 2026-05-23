package io.github.mundanej.mlog.examples.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Minimal consumer example for the mundane logger SLF4J provider. */
public final class Slf4jProviderQuickstart {
  private static final Logger LOG = LoggerFactory.getLogger(Slf4jProviderQuickstart.class);

  private Slf4jProviderQuickstart() {}

  public static void emitUserCreated(String userId) {
    LOG.info("User created {}", userId);
  }
}
