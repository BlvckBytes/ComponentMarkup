package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class ExpressionDrivenNode extends MarkupNode {

  public final ExpressionNode expression;

  public ExpressionDrivenNode(ExpressionNode expression, CursorPosition position) {
    super(position, null, null);

    this.expression = expression;
  }
}
