package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.StringView;
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
        // TODO: Provide better message
        LoggerProvider.log(Level.WARNING, "Could not locate variable " + variableName);
        return null;
      }

      return environment.getVariableValue(variableName);
    }

    return token.getPlainValue();
  }


  @Override
  public StringView getFirstMemberPositionProvider() {
    return token.raw;
  }

  @Override
  public StringView getLastMemberPositionProvider() {
    return token.raw;
  }

  @Override
  public String toExpression() {
    return parenthesise(token.raw.buildString());
  }
}
