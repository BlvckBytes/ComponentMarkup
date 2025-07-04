package at.blvckbytes.component_markup.markup.ast.node.style;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Format {
  OBFUSCATED,
  BOLD,
  STRIKETHROUGH,
  UNDERLINED,
  ITALIC;

  public static final List<Format> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  public static final int COUNT = VALUES.size();
}
