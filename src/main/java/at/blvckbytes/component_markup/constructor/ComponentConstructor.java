/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.constructor;

import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ComponentConstructor {

  boolean doesSupport(ConstructorFeature feature);

  // ================================================================================
  // SlotContext
  // ================================================================================

  SlotContext getSlotContext(SlotType slot);

  // ================================================================================
  // TerminalNode
  // ================================================================================

  Object createTextComponent(String text);

  Object createKeyComponent(String key);

  Object createTranslateComponent(String key, List<Object> with, @Nullable String fallback);

  // ================================================================================
  // Click-Action
  // ================================================================================

  void setClickChangePageAction(Object component, String value);

  void setClickCopyToClipboardAction(Object component, String value);

  void setClickOpenFileAction(Object component, String value);

  void setClickOpenUrlAction(Object component, String value);

  void setClickRunCommandAction(Object component, String value);

  void setClickSuggestCommandAction(Object component, String value);

  // ================================================================================
  // Hover-Action
  // ================================================================================

  void setHoverItemAction(
    Object component,
    String material,
    @Nullable Integer count,
    @Nullable Object name,
    @Nullable List<Object> lore,
    boolean hideProperties
  );

  void setHoverTextAction(Object component, Object text);

  void setHoverEntityAction(Object component, String type, UUID id, @Nullable Object name);

  // ================================================================================
  // Insert-Action
  // ================================================================================

  void setInsertAction(Object component, String value);

  // ================================================================================
  // Styling
  // ================================================================================

  void setColor(Object component, long packedColor);

  void setShadowColor(Object component, long packedColor);

  void setFont(Object component, String font);

  void setObfuscatedFormat(Object component, TriState value);

  void setBoldFormat(Object component, TriState value);

  void setStrikethroughFormat(Object component, TriState value);

  void setUnderlinedFormat(Object component, TriState value);

  void setItalicFormat(Object component, TriState value);

  // ================================================================================
  // Children
  // ================================================================================

  /**
   * Called once a component has been fully constructed and no more changes are to be
   * made. Since Adventure is deeply immutable and thereby enforces builders, this stage
   * would call the build method, as to end up with a final component to pass on.
   * @param component Component to be finalised, as created by the corresponding creation-methods.
   * @return Finalised component, ready to be used wherever applicable.
   */
  Object finaliseComponent(Object component);

  void addChildren(Object component, List<Object> children);
}
