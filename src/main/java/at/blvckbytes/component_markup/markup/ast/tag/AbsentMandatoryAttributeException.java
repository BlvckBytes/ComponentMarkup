package at.blvckbytes.component_markup.markup.ast.tag;

public class AbsentMandatoryAttributeException extends RuntimeException {

  public final AttributeDefinition attribute;

  public AbsentMandatoryAttributeException(AttributeDefinition attribute) {
    this.attribute = attribute;
  }
}
