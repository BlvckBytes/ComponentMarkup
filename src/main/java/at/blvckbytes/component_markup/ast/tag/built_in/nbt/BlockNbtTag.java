package at.blvckbytes.component_markup.ast.tag.built_in.nbt;

public class BlockNbtTag extends NbtTag {

  public BlockNbtTag() {
    super(NbtSource.BLOCK);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("block-nbt");
  }
}
