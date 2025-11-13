/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.constructor;

import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ComponentConstructor<B, C> {

  boolean doesSupport(ConstructorFeature feature);

  // ================================================================================
  // SlotContext
  // ================================================================================

  SlotContext getSlotContext(SlotType slot);

  // ================================================================================
  // TerminalNode
  // ================================================================================

  B createTextComponent(String text);

  B createKeyComponent(String key);

  B createTranslateComponent(String key, List<C> with, @Nullable String fallback);

  // ================================================================================
  // Click-Action
  // ================================================================================

  void setClickChangePageAction(B component, String value);

  void setClickCopyToClipboardAction(B component, String value);

  void setClickOpenFileAction(B component, String value);

  void setClickOpenUrlAction(B component, String value);

  void setClickRunCommandAction(B component, String value);

  void setClickSuggestCommandAction(B component, String value);

  // ================================================================================
  // Hover-Action
  // ================================================================================

  void setHoverItemAction(
    B component,
    String material,
    @Nullable Integer count,
    @Nullable C name,
    @Nullable List<C> lore,
    boolean hideProperties
  );

  void setHoverTextAction(B component, C text);

  void setHoverEntityAction(B component, String type, UUID id, @Nullable C name);

  // ================================================================================
  // Insert-Action
  // ================================================================================

  void setInsertAction(B component, String value);

  // ================================================================================
  // Styling
  // ================================================================================

  void setColor(B component, long packedColor);

  void setShadowColor(B component, long packedColor);

  void setFont(B component, String font);

  void setObfuscatedFormat(B component, TriState value);

  void setBoldFormat(B component, TriState value);

  void setStrikethroughFormat(B component, TriState value);

  void setUnderlinedFormat(B component, TriState value);

  void setItalicFormat(B component, TriState value);

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
  C finaliseComponent(B component);

  void addChildren(B component, List<C> children);
}
