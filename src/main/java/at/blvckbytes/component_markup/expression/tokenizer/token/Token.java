package at.blvckbytes.component_markup.expression.tokenizer.token;

public abstract class Token {

  public final int beginIndex;
  public final int endIndex;

  protected Token(int beginIndex, int endIndex) {
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
  }
}
