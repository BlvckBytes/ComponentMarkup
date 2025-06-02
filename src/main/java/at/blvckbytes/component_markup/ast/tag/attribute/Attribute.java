package at.blvckbytes.component_markup.ast.tag.attribute;

public abstract class Attribute<T> {

  public final String name;

  protected Attribute(String name) {
    this.name = name;
  }

  public abstract T getValue();
}
