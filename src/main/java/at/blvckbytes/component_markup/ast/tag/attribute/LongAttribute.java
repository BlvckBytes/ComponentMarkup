package at.blvckbytes.component_markup.ast.tag.attribute;

public abstract class LongAttribute extends Attribute {

  protected LongAttribute(String name) {
    super(name);
  }

  public abstract long getValue();
}
