package at.blvckbytes.component_markup.ast.tag.built_in.nbt;

public class EntityNbtTag extends NbtTag {

  public EntityNbtTag() {
    super(NbtSource.ENTITY);
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equals("entity-nbt");
  }
}
