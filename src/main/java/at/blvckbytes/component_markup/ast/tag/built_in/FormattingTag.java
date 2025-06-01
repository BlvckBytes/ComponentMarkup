package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.AttributeDefinition;
import at.blvckbytes.component_markup.ast.tag.TagClosing;
import at.blvckbytes.component_markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.ast.tag.TagPriority;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public class FormattingTag implements TagDefinition {

  @Override
  public boolean matchName(String tagName) {
    int nameLength = tagName.length();

    if (nameLength == 0)
      return false;

    char firstChar = tagName.charAt(0);

    if (nameLength == 2 && firstChar == '&') {
      char secondChar = tagName.charAt(1);

      return (
        (secondChar >= 'k' && secondChar <= 'o') ||
        (secondChar >= 'K' && secondChar <= 'O') ||
        secondChar == 'r' ||
        secondChar == 'R'
      );
    }

    switch (tagName) {
      case "b":
      case "bold":
      case "i":
      case "italic":
      case "u":
      case "underlined":
      case "st":
      case "strikethrough":
      case "obf":
      case "obfuscated":
      case "reset":
        return true;
    }

    return false;
  }

  @Override
  public TagClosing getClosing() {
    return TagClosing.OPEN_CLOSE;
  }

  @Override
  public TagPriority getPriority() {
    return TagPriority.NORMAL;
  }

  @Override
  public AttributeDefinition[] getAttributes() {
    return TagDefinition.NO_ATTRIBUTES;
  }

  @Override
  public AstNode construct(String tagName, List<Attribute> attributes, List<AstNode> members) {
    throw new UnsupportedOperationException();
  }
}
