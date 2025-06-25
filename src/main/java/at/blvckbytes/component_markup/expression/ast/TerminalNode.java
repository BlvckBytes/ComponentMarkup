package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;
import org.jetbrains.annotations.Nullable;

public class TerminalNode extends ExpressionNode {

  public final TerminalToken token;

  public TerminalNode(TerminalToken token) {
    super(token.beginIndex);

    this.token = token;
  }

  public @Nullable Object getValue(InterpretationEnvironment environment) {
    if (token instanceof IdentifierToken)
      return environment.getVariableValue((String) token.getPlainValue());

    return token.getPlainValue();
  }
}
