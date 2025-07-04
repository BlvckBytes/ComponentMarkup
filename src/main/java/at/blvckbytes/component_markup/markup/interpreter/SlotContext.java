package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.util.Jsonifiable;

public class SlotContext extends Jsonifiable {

  private static final long DEFAULT_COLOR = AnsiStyleColor.WHITE.packedColor;
  private static final long DEFAULT_SHADOW_COLOR = PackedColor.setClampedA(AnsiStyleColor.BLACK.packedColor, 64);
  private static final String DEFAULT_FONT = "minecraft:default";

  private static final SlotContext SLOT_CHAT = new SlotContext(
    '\n',
    applyCommonDefaults(new ComputedStyle())
  );

  private static final SlotContext SLOT_ENTITY_NAME = new SlotContext(
    ' ',
    applyCommonDefaults(
      new ComputedStyle()
        .setFormat(Format.ITALIC, true)
    )
  );

  private static final SlotContext SLOT_ITEM_LORE = new SlotContext(
    '\0',
    applyCommonDefaults(
      new ComputedStyle()
        .setColor(AnsiStyleColor.DARK_PURPLE.packedColor)
        .setFormat(Format.ITALIC, true)
    )
  );

  private static final SlotContext SLOT_NBT_SEPARATOR = new SlotContext(
    ' ',
    applyCommonDefaults(new ComputedStyle())
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
      case TEXT_TOOLTIP:
        return SLOT_ENTITY_NAME;

      case ITEM_LORE:
        return SLOT_ITEM_LORE;

      case NBT_SEPARATOR:
      case SELECTOR_SEPARATOR:
      case TRANSLATE_WITH:
        return SLOT_NBT_SEPARATOR;
    }

    return SLOT_CHAT;
  }

  private static ComputedStyle applyCommonDefaults(ComputedStyle input) {
    if (input.packedColor == PackedColor.NULL_SENTINEL)
      input.packedColor = DEFAULT_COLOR;

    if (input.packedShadowColor == PackedColor.NULL_SENTINEL)
      input.packedShadowColor = DEFAULT_SHADOW_COLOR;

    if (input.font == null)
      input.font = DEFAULT_FONT;

    if (input.formats == null)
      input.formats = new Boolean[Format.VALUES.size()];

    for (Format format : Format.VALUES) {
      if (input.formats[format.ordinal()] == null)
        input.formats[format.ordinal()] = false;
    }

    return input;
  }
}
