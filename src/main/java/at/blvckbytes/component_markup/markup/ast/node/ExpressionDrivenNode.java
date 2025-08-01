package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringView;

public class ExpressionDrivenNode extends MarkupNode {

  public final ExpressionNode expression;

  public ExpressionDrivenNode(StringView positionProvider, ExpressionNode expression) {
    super(positionProvider, null, null);

    this.expression = expression;
  }
}
