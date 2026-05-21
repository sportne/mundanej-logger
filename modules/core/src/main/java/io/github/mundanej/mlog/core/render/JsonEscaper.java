package io.github.mundanej.mlog.core.render;

/** JSON string escaping helper. */
public final class JsonEscaper {
  private JsonEscaper() {}

  public static void appendQuoted(StringBuilder builder, String value) {
    if (value == null) {
      builder.append("null");
      return;
    }
    builder.append('"');
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      switch (character) {
        case '"' -> builder.append("\\\"");
        case '\\' -> builder.append("\\\\");
        case '\b' -> builder.append("\\b");
        case '\f' -> builder.append("\\f");
        case '\n' -> builder.append("\\n");
        case '\r' -> builder.append("\\r");
        case '\t' -> builder.append("\\t");
        default -> {
          if (character < 0x20) {
            builder.append("\\u");
            String hex = Integer.toHexString(character);
            builder.append("0".repeat(4 - hex.length()));
            builder.append(hex);
          } else {
            builder.append(character);
          }
        }
      }
    }
    builder.append('"');
  }
}
