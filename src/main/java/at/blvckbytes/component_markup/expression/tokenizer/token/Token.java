package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.util.JsonifyIgnore;

public abstract class Token extends Jsonifiable {

  public final int beginIndex;

  // TODO: Implement me
  @JsonifyIgnore
  public final int endIndex = -1;

  protected Token(int beginIndex) {
    this.beginIndex = beginIndex;
  }
}
