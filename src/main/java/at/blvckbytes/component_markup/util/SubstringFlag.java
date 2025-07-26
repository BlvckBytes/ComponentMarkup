package at.blvckbytes.component_markup.util;

import java.util.EnumSet;

public enum SubstringFlag {
  REMOVE_NEWLINES_INDENT,
  REMOVE_LEADING_SPACE,
  REMOVE_TRAILING_SPACE,
  ;

  public static final EnumSet<SubstringFlag> INNER_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT);
  public static final EnumSet<SubstringFlag> FIRST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_LEADING_SPACE);
  public static final EnumSet<SubstringFlag> LAST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_TRAILING_SPACE);
  public static final EnumSet<SubstringFlag> ONLY_TEXT = EnumSet.allOf(SubstringFlag.class);

}
