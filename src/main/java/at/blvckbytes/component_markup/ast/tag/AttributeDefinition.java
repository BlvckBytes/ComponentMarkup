package at.blvckbytes.component_markup.ast.tag;

public class AttributeDefinition {

  public final String name;
  public final AttributeType type;
  public final boolean multiValue;
  public final boolean mandatory;

  public AttributeDefinition(
    String name,
    AttributeType type,
    boolean multiValue,
    boolean mandatory
  ) {
    this.name = name;
    this.type = type;
    this.multiValue = multiValue;
    this.mandatory = mandatory;
  }
}
