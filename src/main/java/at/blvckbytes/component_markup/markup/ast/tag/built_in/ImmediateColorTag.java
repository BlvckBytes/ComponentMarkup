package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.interpreter.AnsiStyleColor;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ImmediateColorTag extends TagDefinition {

  public ImmediateColorTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagNameToColor(tagName) != null;
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(tagName.startInclusive, children, letBindings);

    ExpressionNode color = tagNameToColor(tagName);

    if (color != null)
      wrapper.getOrInstantiateStyle().color = color;

    return wrapper;
  }

  private @Nullable ExpressionNode tagNameToColor(StringView tagName) {
    int nameLength = tagName.length();

    if (nameLength == 0)
      return null;

    char firstChar = tagName.nthChar(0);

    AnsiStyleColor color;

    if (nameLength == 2 && firstChar == '&') {
      if ((color = AnsiStyleColor.fromCharOrNull(tagName.nthChar(1))) != null)
        return ImmediateExpression.ofString(color.name);
    }

    if (nameLength == 7 && firstChar == '#') {
      for (int charIndex = 1; charIndex < 7; ++charIndex) {
        if (!isHexadecimalChar(tagName.nthChar(charIndex)))
          return null;
      }

      return ImmediateExpression.ofString(tagName);
    }

    if ((color = AnsiStyleColor.fromNameLowerOrNull(tagName.buildString())) != null)
      return ImmediateExpression.ofString(color.name);

    return null;
  }

  private boolean isHexadecimalChar(char c) {
    return (c >= 'a' && c <= 'f') || (c >= '0' && c <= '9');
  }
}
