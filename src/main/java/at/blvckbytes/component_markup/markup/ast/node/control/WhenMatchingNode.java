package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import org.jetbrains.annotations.Nullable;

public class WhenMatchingNode extends MarkupNode {

  public final ExpressionNode input;
  public final WhenMatchingMap matchingMap;
  public final @Nullable MarkupNode other;

  public WhenMatchingNode(
    int position,
    ExpressionNode input,
    WhenMatchingMap matchingMap,
    @Nullable MarkupNode other
  ) {
    super(position, null, null);

    this.input = input;
    this.matchingMap = matchingMap;
    this.other = other;
  }
}
