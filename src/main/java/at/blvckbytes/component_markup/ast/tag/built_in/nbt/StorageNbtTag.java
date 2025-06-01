package at.blvckbytes.component_markup.ast.tag.built_in.nbt;

public class StorageNbtTag extends NbtTag {

  protected StorageNbtTag() {
    super(NbtSource.STORAGE);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("storage-nbt");
  }
}
