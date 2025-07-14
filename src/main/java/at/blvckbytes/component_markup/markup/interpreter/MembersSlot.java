package at.blvckbytes.component_markup.markup.interpreter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum MembersSlot {
  CHILDREN,
  TRANSLATE_WITH,
  HOVER_ENTITY_NAME,
  HOVER_TEXT_VALUE,
  HOVER_ITEM_NAME,
  HOVER_ITEM_LORE,
  ;

  public static final List<MembersSlot> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
}
