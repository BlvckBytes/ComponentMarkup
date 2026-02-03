package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ColorTag extends TagDefinition {

  public ColorTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("color", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(tagName, children, letBindings);

    ExpressionNode valueNode = attributes.getOptionalExpressionNode("value");

    if (valueNode == null) {
      List<String> unusedNames = attributes.getUnusedNamesInOrder();

      if (!unusedNames.isEmpty())
        valueNode = attributes.getMandatoryExpressionNode(unusedNames.get(0));
      else
        valueNode = attributes.getMandatoryExpressionNode("value");
    }

    wrapper.getOrInstantiateStyle().color = valueNode;

    return wrapper;
  }
}
