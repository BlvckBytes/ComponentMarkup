package at.blvckbytes.component_markup.markup.ast.tag.built_in.selector;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class SelectorTag extends TagDefinition {

  public SelectorTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals("selector", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new SelectorNode(
      attributes.getMandatoryExpressionNode("selector"),
      attributes.getOptionalMarkupNode("separator"),
      tagName.viewStart, letBindings
    );
  }
}
