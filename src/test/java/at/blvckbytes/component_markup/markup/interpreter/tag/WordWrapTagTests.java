package at.blvckbytes.component_markup.markup.interpreter.tag;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.markup.interpreter.InterpreterTestsBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class WordWrapTagTests extends InterpreterTestsBase {

  private static final List<String> variousItemNames = Arrays.asList(
    "Pink Terracotta", "Red Sand", "Diamond Ore", "Golden Leggings", "Deepslate Emerald Ore", "Raw Salmon",
    "Copper Nautilus Armor", "Raw Cod", "Wet Sponge", "Deepslate Coal Ore", "Nether Gold Ore", "Golden Axe",
    "Golden Spear", "Light Gray Terracotta", "Sandstone", "Copper Pickaxe", "Iron Horse Armor", "Birch Leaves",
    "Golden Boots", "Deepslate Gold Ore", "Deepslate Iron Ore", "Raw Rabbit", "Purple Terracotta", "Iron Helmet",
    "Copper Ore", "Sea Pickle", "Golden Sword", "Copper Sword", "Iron Leggings", "Iron Sword", "Golden Pickaxe"
  );

  @Test
  public void shouldWrapABunchOfItemNames() {
    makeRecordedCase(
      new TextWithSubViews(
        "<word-wrap",
        "  width=35",
        "  value-separator={<&7>,<space/>}",
        "  token-renderer={<&6>{token}}",
        "  [...value]='values'",
        "/>"
      ),
      new InterpretationEnvironment()
        .withVariable("values", variousItemNames),
      SlotType.ITEM_LORE
    );
  }
}
