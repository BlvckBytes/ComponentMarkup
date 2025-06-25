package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.util.Jsonifiable;

public abstract class Token extends Jsonifiable {

  public final int beginIndex;

  protected Token(int beginIndex) {
    this.beginIndex = beginIndex;
  }
}
