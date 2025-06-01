package at.blvckbytes.component_markup.ast.tag.attribute;

public abstract class DoubleAttribute extends Attribute {

  protected DoubleAttribute(String name) {
    super(name);
  }

  public abstract double getValue();
}
