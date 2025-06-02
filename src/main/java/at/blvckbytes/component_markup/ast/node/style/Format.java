package at.blvckbytes.component_markup.ast.node.style;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Format {
  MAGIC,
  BOLD,
  STRIKETHROUGH,
  UNDERLINED,
  ITALIC;

  public static final List<Format> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
}
