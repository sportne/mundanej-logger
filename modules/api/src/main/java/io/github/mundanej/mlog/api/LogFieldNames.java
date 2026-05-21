package io.github.mundanej.mlog.api;

import java.util.Objects;
import java.util.regex.Pattern;

/** Field-name validation shared by native API, core, and SLF4J integration. */
public final class LogFieldNames {
  private static final Pattern VALID_FIELD_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_.-]{0,127}");

  private LogFieldNames() {}

  /** Validates and returns a structured field name. */
  public static String requireValid(String name) {
    Objects.requireNonNull(name, "name");
    if (!VALID_FIELD_NAME.matcher(name).matches()) {
      throw new IllegalArgumentException(
          "field name must match [A-Za-z_][A-Za-z0-9_.-]{0,127}: " + name);
    }
    return name;
  }
}
