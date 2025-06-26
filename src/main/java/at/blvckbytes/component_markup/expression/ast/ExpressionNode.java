package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.util.JsonifyIgnore;

public abstract class ExpressionNode extends Jsonifiable {

  public final int beginIndex;

  // TODO: Implement me
  @JsonifyIgnore
  public final int endIndex = -1;

  protected ExpressionNode(int beginIndex) {
    this.beginIndex = beginIndex;
  }
}
