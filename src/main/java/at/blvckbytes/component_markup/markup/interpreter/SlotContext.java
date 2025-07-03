package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.util.Jsonifiable;

public class SlotContext extends Jsonifiable {

  private static final String DEFAULT_FONT = "minecraft:default";
  private static final long DEFAULT_SHADOW_COLOR = PackedColor.setClampedA(AnsiStyleColor.BLACK.packedColor, 64);

  private static final SlotContext SLOT_CHAT = new SlotContext(
    '\n',
    new ComputedStyle()
      .setColor(AnsiStyleColor.WHITE.packedColor)
      .setShadowColor(DEFAULT_SHADOW_COLOR)
      .setFont(DEFAULT_FONT)
  );

  private static final SlotContext SLOT_ENTITY_NAME = new SlotContext(
    ' ',
    new ComputedStyle()
      .setColor(AnsiStyleColor.WHITE.packedColor)
      .setFormat(Format.ITALIC, true)
      .setShadowColor(DEFAULT_SHADOW_COLOR)
      .setFont(DEFAULT_FONT)
  );

  private static final SlotContext SLOT_ITEM_LORE = new SlotContext(
    ' ',
    new ComputedStyle()
      .setColor(AnsiStyleColor.DARK_PURPLE.packedColor)
      .setFormat(Format.ITALIC, true)
      .setShadowColor(DEFAULT_SHADOW_COLOR)
      .setFont(DEFAULT_FONT)
  );

  private static final SlotContext SLOT_NBT_SEPARATOR = new SlotContext(
    ' ',
    new ComputedStyle()
      .setColor(AnsiStyleColor.WHITE.packedColor)
      .setShadowColor(DEFAULT_SHADOW_COLOR)
      .setFont(DEFAULT_FONT)
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
}
