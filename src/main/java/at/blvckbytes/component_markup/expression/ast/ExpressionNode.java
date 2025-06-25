package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.Jsonifiable;

public abstract class ExpressionNode extends Jsonifiable {

  public final int beginIndex;

  protected ExpressionNode(int beginIndex) {
    this.beginIndex = beginIndex;
  }
}
