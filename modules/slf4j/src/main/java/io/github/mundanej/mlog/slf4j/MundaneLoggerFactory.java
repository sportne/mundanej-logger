package io.github.mundanej.mlog.slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

final class MundaneLoggerFactory implements ILoggerFactory {
  private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();
  private final MundaneMdcAdapter mdcAdapter;

  MundaneLoggerFactory(MundaneMdcAdapter mdcAdapter) {
    this.mdcAdapter = mdcAdapter;
  }

  @Override
  public Logger getLogger(String name) {
    return loggers.computeIfAbsent(
        name, loggerName -> new MundaneSlf4jLogger(loggerName, mdcAdapter));
  }
}
