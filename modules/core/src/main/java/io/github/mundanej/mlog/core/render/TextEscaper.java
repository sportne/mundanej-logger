package io.github.mundanej.mlog.core.render;

/** Escapes text so each event remains one physical line. */
public final class TextEscaper {
  private TextEscaper() {}

  public static String escape(String value) {
    if (value == null) {
      return "null";
    }
    StringBuilder builder = new StringBuilder(value.length());
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      switch (character) {
        case '\n' -> builder.append("\\n");
        case '\r' -> builder.append("\\r");
        case '\t' -> builder.append("\\t");
        case '\\' -> builder.append("\\\\");
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
    return builder.toString();
  }
}
