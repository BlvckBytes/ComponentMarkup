package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.markup.ast.tag.MandatoryExpressionAttributeDefinition;

public class BlockNbtTag extends NbtTag {

  public BlockNbtTag() {
    super(
      NbtSource.BLOCK,
      "block-nbt",
      new MandatoryExpressionAttributeDefinition("coordinates")
    );
  }
}
