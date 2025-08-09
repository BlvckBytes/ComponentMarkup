/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.tokenizer.InterpolationMember;
import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.StringToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
        for (String line : ErrorScreen.make(token.raw, "Could not locate variable \"" + variableName + "\""))
          LoggerProvider.log(Level.WARNING, line, false);

        return null;
      }

      return environment.getVariableValue(variableName);
    }

    if (token instanceof StringToken) {
      List<InterpolationMember> members = ((StringToken) token).members;

      String plainValue;

      if ((plainValue = (String) token.getPlainValue()) != null)
        return plainValue;

      StringBuilder result = new StringBuilder();

      for (Object member : members) {
        if (member instanceof StringView) {
          result.append(((StringView) member).buildString());
          continue;
        }

        if (member instanceof ExpressionNode) {
          result.append(ExpressionInterpreter.interpret((ExpressionNode) member, environment));
          continue;
        }

        LoggerProvider.log(Level.WARNING, "Encountered unknown interpolation-member: " + (member == null ? null : member.getClass()));
      }

      return result.toString();
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
