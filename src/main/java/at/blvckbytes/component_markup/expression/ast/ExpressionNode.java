package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.Jsonifiable;

public abstract class ExpressionNode extends Jsonifiable {

  public final int beginIndex;
  public final int endIndex;

  protected ExpressionNode(int beginIndex, int endIndex) {
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
  }
}
