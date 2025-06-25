package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.util.Jsonifiable;

public abstract class ExpressionToken extends Jsonifiable {

  public final int beginIndex;

  protected ExpressionToken(int beginIndex) {
    this.beginIndex = beginIndex;
  }
}
