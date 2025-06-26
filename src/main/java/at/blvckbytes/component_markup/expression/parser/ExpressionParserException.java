package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import org.jetbrains.annotations.Nullable;

public class ExpressionParserException extends RuntimeException {

  public final ExpressionParserError error;
  public final @Nullable Token token;

  public ExpressionParserException(ExpressionParserError error, @Nullable Token token) {
    this.error = error;
    this.token = token;
  }
}
