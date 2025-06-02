package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public class ColorTag extends TagDefinition {

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
    return NO_ATTRIBUTES;
  }

  @Override
  public AstNode construct(
    String tagName,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    ContentNode wrapper = new ContentNode(children, letBindings);
    wrapper.style.color = tagNameToColor(tagName);
    return wrapper;
  }

  private String tagNameToColor(String tagName) {
    int nameLength = tagName.length();

    if (nameLength > 2) {
      switch (tagName) {
        case "grey":
          return "gray";

        case "dark_grey":
          return "dark_gray";
      }

      return tagName;
    }

    if (nameLength != 2)
      return tagName;

    if (tagName.charAt(0) != '&')
      return tagName;

    switch (tagName.charAt(1)) {
      case 'a':
      case 'A':
        return "green";
      case 'b':
      case 'B':
        return "aqua";
      case 'c':
      case 'C':
        return "red";
      case 'd':
      case 'D':
        return "light_purple";
      case 'e':
      case 'E':
        return "yellow";
      case 'f':
      case 'F':
        return "white";
      case '0':
        return "black";
      case '1':
        return "dark_blue";
      case '2':
        return "dark_green";
      case '3':
        return "dark_aqua";
      case '4':
        return "dark_red";
      case '5':
        return "dark_purple";
      case '6':
        return "gold";
      case '7':
        return "gray";
      case '8':
        return "dark_gray";
      case '9':
        return "blue";
    }

    return tagName;
  }

  private boolean isHexadecimalChar(char c) {
    return (
      (c >= 'a' && c <= 'f') ||
      (c >= 'A' && c <= 'F') ||
      (c >= '0' && c <= '9')
    );
  }
}
