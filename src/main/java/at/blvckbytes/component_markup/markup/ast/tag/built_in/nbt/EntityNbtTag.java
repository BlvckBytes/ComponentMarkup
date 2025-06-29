package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.markup.ast.tag.MandatoryExpressionAttributeDefinition;

public class EntityNbtTag extends NbtTag {

  public EntityNbtTag() {
    super(
      NbtSource.ENTITY,
      "entity-nbt",
      new MandatoryExpressionAttributeDefinition("selector")
    );
  }
}
