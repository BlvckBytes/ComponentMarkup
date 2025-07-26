package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ResetTag extends TagDefinition {

  protected ResetTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals("r", true) || tagName.contentEquals("reset", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ContainerNode result = new ContainerNode(tagName.viewStart, children, letBindings);
    result.getOrInstantiateStyle().reset = ImmediateExpression.ofBoolean(tagName, true);
    return result;
  }
}
