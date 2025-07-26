package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;

public class ExpressionDrivenNode extends MarkupNode {

  public final ExpressionNode expression;

  public ExpressionDrivenNode(ExpressionNode expression) {
    super(expression.getBegin(), null, null);

    this.expression = expression;
  }
}
