/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.constructor;

import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.interpreter.ComputedStyle;
import at.blvckbytes.component_markup.util.TriState;
import at.blvckbytes.component_markup.util.TriStateBitFlags;
import at.blvckbytes.component_markup.util.color.AnsiStyleColor;
import at.blvckbytes.component_markup.util.color.PackedColor;

import java.util.function.Supplier;

public class SlotContext {

  private static final long DEFAULT_COLOR = AnsiStyleColor.WHITE.packedColor;
  private static final int DEFAULT_SHADOW_COLOR_OPACITY = 63;
  private static final long DEFAULT_SHADOW_COLOR = PackedColor.setClampedA(AnsiStyleColor.BLACK.packedColor, DEFAULT_SHADOW_COLOR_OPACITY);
  private static final String DEFAULT_FONT = "minecraft:default";

  private static final SlotContext SLOT_CHAT = new SlotContext(
    '\n',
    applyCommonDefaults(ComputedStyle::new)
  );

  private static final SlotContext SLOT_INVENTORY_TITLE = new SlotContext(
    ' ',
    applyCommonDefaults(() -> {
      ComputedStyle style = new ComputedStyle();
      style.packedColor = AnsiStyleColor.DARK_GRAY.packedColor;
      return style;
    })
  );

  private static final SlotContext SLOT_ENTITY_NAME = new SlotContext(
    ' ',
    applyCommonDefaults(() -> {
      ComputedStyle style = new ComputedStyle();
      style.formats = TriStateBitFlags.write(style.formats, Format.ITALIC.ordinal(), TriState.TRUE);
      return style;
    })
  );

  private static final SlotContext SLOT_ITEM_LORE = new SlotContext(
    '\0',
    applyCommonDefaults(() -> {
      ComputedStyle style = new ComputedStyle();
      style.formats = TriStateBitFlags.write(style.formats, Format.ITALIC.ordinal(), TriState.TRUE);
      style.packedColor = AnsiStyleColor.DARK_PURPLE.packedColor;
      return style;
    })
  );

  private static final SlotContext SLOT_SINGLE_LINE_CHAT = new SlotContext(
    ' ',
    applyCommonDefaults(ComputedStyle::new)
  );

  public final char breakChar;
  public final ComputedStyle defaultStyle;

  public SlotContext(char breakChar, ComputedStyle defaultStyle) {
    this.breakChar = breakChar;
    this.defaultStyle = defaultStyle;
  }

  public static SlotContext getForSlot(SlotType slot) {
    switch (slot) {
      case ENTITY_NAME:
      case ITEM_NAME:
        return SLOT_ENTITY_NAME;

      case ITEM_LORE:
        return SLOT_ITEM_LORE;

      case SINGLE_LINE_CHAT:
        return SLOT_SINGLE_LINE_CHAT;

      case INVENTORY_TITLE:
        return SLOT_INVENTORY_TITLE;
    }

    return SLOT_CHAT;
  }

  private static ComputedStyle applyCommonDefaults(Supplier<ComputedStyle> inputSupplier) {
    ComputedStyle input = inputSupplier.get();

    if (input.packedColor == PackedColor.NULL_SENTINEL)
      input.packedColor = DEFAULT_COLOR;

    if (input.packedShadowColor == PackedColor.NULL_SENTINEL) {
      input.packedShadowColor = DEFAULT_SHADOW_COLOR;
      input.packedShadowColorOpacity = DEFAULT_SHADOW_COLOR_OPACITY;
    }

    if (input.font == null)
      input.font = DEFAULT_FONT;

    for (int index = 0; index < Format.COUNT; ++index) {
      if (TriStateBitFlags.read(input.formats, index) == TriState.NULL)
        input.formats = TriStateBitFlags.write(input.formats, index, TriState.FALSE);
    }

    return input;
  }
}
