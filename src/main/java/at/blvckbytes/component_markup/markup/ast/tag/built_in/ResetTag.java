package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ResetTag extends TagDefinition {

  protected ResetTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("r") || tagNameLower.equals("reset");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable Set<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ContainerNode result = new ContainerNode(position, children, letBindings);
    result.getOrInstantiateStyle().reset = ImmediateExpression.of(true);
    return result;
  }
}
