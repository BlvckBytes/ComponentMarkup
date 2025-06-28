package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

public enum NbtSource {
  BLOCK("coordinates"),
  ENTITY("selector"),
  STORAGE("key")
  ;

  public final String attributeName;

  NbtSource(String attributeName) {
    this.attributeName = attributeName;
  }
}
