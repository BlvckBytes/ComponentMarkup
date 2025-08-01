package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class WhenMatchingNode extends MarkupNode {

  public final ExpressionNode input;
  public final WhenMatchingMap matchingMap;
  public final @Nullable MarkupNode other;

  public WhenMatchingNode(
    StringView positionProvider,
    ExpressionNode input,
    WhenMatchingMap matchingMap,
    @Nullable MarkupNode other
  ) {
    super(positionProvider, null, null);

    this.input = input;
    this.matchingMap = matchingMap;
    this.other = other;
  }
}
