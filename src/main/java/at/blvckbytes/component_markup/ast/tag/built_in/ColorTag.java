package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.AttributeDefinition;
import at.blvckbytes.component_markup.ast.tag.TagClosing;
import at.blvckbytes.component_markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.ast.tag.TagPriority;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public class ColorTag implements TagDefinition {

  @Override
  public boolean matchName(String tagName) {
    int nameLength = tagName.length();

    if (nameLength == 0)
      return false;

    char firstChar = tagName.charAt(0);

    if (nameLength == 2 && firstChar == '&')
      return isHexadecimalChar(tagName.charAt(1));

    switch (tagName) {
      case "black":
      case "dark_blue":
      case "dark_green":
      case "dark_aqua":
      case "dark_red":
      case "dark_purple":
      case "gold":
      case "gray":
      case "grey":
      case "dark_gray":
      case "dark_grey":
      case "blue":
      case "green":
      case "aqua":
      case "red":
      case "light_purple":
      case "yellow":
      case "white":
        return true;
    }

    if (nameLength == 7 && firstChar == '#') {
      for (int charIndex = 1; charIndex < 7; ++charIndex) {
        if (!isHexadecimalChar(tagName.charAt(charIndex)))
          return false;
      }

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

  private boolean isHexadecimalChar(char c) {
    return (
      (c >= 'a' && c <= 'f') ||
        (c >= 'A' && c <= 'F') ||
        (c >= '0' && c <= '9')
    );
  }
}
