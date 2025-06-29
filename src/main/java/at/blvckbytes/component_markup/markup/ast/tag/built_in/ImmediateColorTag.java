package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImmediateColorTag extends TagDefinition {

  private static final String[] staticPrefixes;

  static {
    staticPrefixes = new String[AnsiStyleColor.NAMES.size() + 2];

    int prefixIndex = 0;

    for (String namedColor : AnsiStyleColor.NAMES)
      staticPrefixes[prefixIndex++] = namedColor;

    staticPrefixes[prefixIndex++] = "&";
    staticPrefixes[prefixIndex] = "#";
  }

  public ImmediateColorTag() {
    super(NO_ATTRIBUTES, staticPrefixes, TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameToColor(tagNameLower) != null;
  }

  @Override
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);

    String color = tagNameToColor(tagNameLower);

    if (color != null)
      wrapper.getOrInstantiateStyle().color = ImmediateExpression.of(color);

    return wrapper;
  }

  private @Nullable String tagNameToColor(String tagNameLower) {
    int nameLength = tagNameLower.length();

    if (nameLength == 0)
      return null;

    char firstChar = tagNameLower.charAt(0);

    AnsiStyleColor color;

    if (nameLength == 2 && firstChar == '&') {
      if ((color = AnsiStyleColor.fromChar(tagNameLower.charAt(1))) != null)
        return color.name;
    }

    if (nameLength == 7 && firstChar == '#') {
      for (int charIndex = 1; charIndex < 7; ++charIndex) {
        if (!isHexadecimalChar(tagNameLower.charAt(charIndex)))
          return null;
      }

      return tagNameLower;
    }

    if ((color = AnsiStyleColor.fromName(tagNameLower)) != null)
      return color.name;

    return null;
  }

  private boolean isHexadecimalChar(char c) {
    return (c >= 'a' && c <= 'f') || (c >= '0' && c <= '9');
  }
}
