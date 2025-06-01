package at.blvckbytes.component_markup.ast.tag;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public interface TagDefinition {

  AttributeDefinition[] NO_ATTRIBUTES = new AttributeDefinition[0];

  boolean matchName(String tagName);

  TagClosing getClosing();

  TagPriority getPriority();

  AttributeDefinition[] getAttributes();

  AstNode construct(String tagName, List<Attribute> attributes, List<AstNode> members);

}
