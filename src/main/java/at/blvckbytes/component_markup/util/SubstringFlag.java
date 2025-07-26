package at.blvckbytes.component_markup.util;

import java.util.EnumSet;

public enum SubstringFlag {
  REMOVE_NEWLINES_INDENT,
  REMOVE_LEADING_SPACE,
  REMOVE_TRAILING_SPACE,
  ;

  public static final EnumSet<SubstringFlag> NONE = EnumSet.noneOf(SubstringFlag.class);
}
