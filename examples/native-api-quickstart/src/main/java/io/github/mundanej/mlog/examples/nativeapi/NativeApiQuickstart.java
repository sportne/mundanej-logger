package io.github.mundanej.mlog.examples.nativeapi;

import io.github.mundanej.mlog.api.MundaneLogger;
import io.github.mundanej.mlog.core.MundaneLogManager;

/** Minimal consumer example for the native mundane logger API. */
public final class NativeApiQuickstart {
  private static final MundaneLogger LOG = MundaneLogger.get(NativeApiQuickstart.class);

  private NativeApiQuickstart() {}

  public static void main(String[] args) {
    MundaneLogManager.installDefault();
    emitUserCreated("user-1");
  }

  public static void emitUserCreated(String userId) {
    LOG.info().event("user.created").field("userId", userId).message("User created").emit();
  }
}
