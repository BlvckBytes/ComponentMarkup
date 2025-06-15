package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImmediateColorTag extends TagDefinition {

  private static final Set<String> namedColors;
  private static final String[] staticPrefixes;

  static {
    namedColors = new HashSet<>();
    namedColors.add("black");
    namedColors.add("dark_blue");
    namedColors.add("dark_green");
    namedColors.add("dark_aqua");
    namedColors.add("dark_red");
    namedColors.add("dark_purple");
    namedColors.add("gold");
    namedColors.add("gray");
    namedColors.add("grey");
    namedColors.add("dark_gray");
    namedColors.add("dark_grey");
    namedColors.add("blue");
    namedColors.add("green");
    namedColors.add("aqua");
    namedColors.add("red");
    namedColors.add("light_purple");
    namedColors.add("yellow");
    namedColors.add("white");

    staticPrefixes = new String[namedColors.size() + 2];

    int prefixIndex = 0;

    for (String namedColor : namedColors)
      staticPrefixes[prefixIndex++] = namedColor;

    staticPrefixes[prefixIndex++] = "&";
    staticPrefixes[prefixIndex] = "#";
  }

  public ImmediateColorTag() {
    super(NO_ATTRIBUTES, staticPrefixes, TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagName) {
    int nameLength = tagName.length();

    if (nameLength == 0)
      return false;

    char firstChar = tagName.charAt(0);

    if (nameLength == 2 && firstChar == '&')
      return isHexadecimalChar(tagName.charAt(1));

    if (namedColors.contains(tagName))
      return true;

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
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    wrapper.style.color = ImmediateExpression.of(tagNameToColor(tagName));
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
