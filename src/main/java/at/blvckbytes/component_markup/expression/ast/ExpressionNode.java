package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.util.JsonifyGetter;

public abstract class ExpressionNode extends Jsonifiable {

  protected ExpressionNode() {}

  @JsonifyGetter
  public abstract int getBeginIndex();

  @JsonifyGetter
  public abstract int getEndIndex();
}
