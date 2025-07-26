package at.blvckbytes.component_markup.markup.ast.node.click;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.click.ClickAction;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringPosition;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ClickNode extends MarkupNode {

  public final ClickAction action;
  public final ExpressionNode value;

  public ClickNode(
    ClickAction action,
    ExpressionNode value,
    StringPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.action = action;
    this.value = value;
  }
}
