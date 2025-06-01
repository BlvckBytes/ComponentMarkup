package at.blvckbytes.component_markup.ast.tag.attribute;

public abstract class StringAttribute extends Attribute {

  protected StringAttribute(String name) {
    super(name);
  }

  public abstract String getValue();
}
