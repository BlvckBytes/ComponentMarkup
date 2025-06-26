package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.util.Jsonifiable;

public abstract class Token extends Jsonifiable {

  public final int beginIndex;
  public final int endIndex;

  protected Token(int beginIndex, int endIndex) {
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
  }
}
