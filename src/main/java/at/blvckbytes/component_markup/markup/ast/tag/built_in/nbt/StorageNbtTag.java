package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.markup.ast.tag.MandatoryExpressionAttributeDefinition;

public class StorageNbtTag extends NbtTag {

  public StorageNbtTag() {
    super(
      NbtSource.STORAGE,
      "storage-nbt",
      new MandatoryExpressionAttributeDefinition("key")
    );
  }
}
