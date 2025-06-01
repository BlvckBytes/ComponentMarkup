package at.blvckbytes.component_markup.ast.tag.attribute;

public abstract class BooleanAttribute extends Attribute {

  protected BooleanAttribute(String name) {
    super(name);
  }

  public abstract boolean getValue();
}
