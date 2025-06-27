package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;
import org.jetbrains.annotations.Nullable;

public class TerminalNode extends ExpressionNode {

  public final TerminalToken token;

  public TerminalNode(TerminalToken token) {
    this.token = token;
  }

  public @Nullable Object getValue(InterpretationEnvironment environment) {
    if (token instanceof IdentifierToken)
      return environment.getVariableValue((String) token.getPlainValue());

    return token.getPlainValue();
  }

  @Override
  public int getBeginIndex() {
    return token.beginIndex;
  }

  @Override
  public int getEndIndex() {
    return token.endIndex;
  }
}
