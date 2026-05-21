package io.github.mundanej.mlog.slf4j;

import io.github.mundanej.mlog.api.MundaneLogBackend;
import io.github.mundanej.mlog.core.MundaneLogManager;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/** SLF4J 2.x service provider for mundane logger. */
public final class MundaneSlf4jServiceProvider implements SLF4JServiceProvider {
  private static final String REQUESTED_API_VERSION = "2.0.99";

  private final MundaneMdcAdapter mdcAdapter = new MundaneMdcAdapter();
  private final IMarkerFactory markerFactory = new BasicMarkerFactory();
  private final MundaneLoggerFactory loggerFactory = new MundaneLoggerFactory(mdcAdapter);

  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  @Override
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  @Override
  public MDCAdapter getMDCAdapter() {
    return mdcAdapter;
  }

  @Override
  public String getRequestedApiVersion() {
    return REQUESTED_API_VERSION;
  }

  @Override
  public void initialize() {
    if (MundaneLogBackend.isNoOpInstalled()) {
      MundaneLogManager.installDefault();
    }
  }
}
