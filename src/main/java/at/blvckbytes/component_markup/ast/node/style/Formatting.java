package at.blvckbytes.component_markup.ast.node.style;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Formatting {
  MAGIC,
  BOLD,
  STRIKETHROUGH,
  UNDERLINE,
  ITALIC;

  public static final List<Formatting> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
}
