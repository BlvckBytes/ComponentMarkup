package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class TerminalNode extends ExpressionNode {

  public TerminalToken token;

  public TerminalNode(TerminalToken token) {
    this.token = token;
  }

  public @Nullable Object getValue(InterpretationEnvironment environment) {
    if (token instanceof IdentifierToken) {
      String variableName = (String) token.getPlainValue();

      if (!environment.doesVariableExist(variableName)) {
        LoggerProvider.get().log(Level.WARNING, "Could not access variable " + variableName);
        return null;
      }

      return environment.getVariableValue(variableName);
    }

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

  @Override
  public String toExpression() {
    return parenthesise(token.raw);
  }
}
