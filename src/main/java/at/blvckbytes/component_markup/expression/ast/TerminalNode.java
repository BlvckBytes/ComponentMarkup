/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.tokenizer.InterpolationMember;
import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TemplateLiteralToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.logging.GlobalLogger;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;
import java.util.logging.Level;

public class TerminalNode extends ExpressionNode {

  public TerminalToken token;

  public TerminalNode(TerminalToken token) {
    this.token = token;
  }

  public @Nullable Object getValue(InterpretationEnvironment environment, InterpreterLogger logger) {
    if (token instanceof IdentifierToken) {
      String variableName = (String) token.getPlainValue();

      if (!environment.doesVariableExist(variableName)) {
        StringJoiner knownNamesJoiner = new StringJoiner(", ");
        environment.forEachKnownName(knownNamesJoiner::add);
        logger.logErrorScreen(token.raw, "Could not locate variable \"" + variableName + "\"; choose one of: " + knownNamesJoiner);
        return null;
      }

      return environment.getVariableValue(variableName);
    }

    if (token instanceof TemplateLiteralToken) {
      StringBuilder result = new StringBuilder();

      for (InterpolationMember member : ((TemplateLiteralToken) token).members) {
        if (member instanceof InputView) {
          result.append(((InputView) member).buildString());
          continue;
        }

        if (member instanceof ExpressionNode) {
          result.append(ExpressionInterpreter.interpret((ExpressionNode) member, environment, logger));
          continue;
        }

        GlobalLogger.log(Level.WARNING, "Encountered unknown interpolation-member: " + (member == null ? null : member.getClass()));
      }

      return result.toString();
    }

    return token.getPlainValue();
  }


  @Override
  public InputView getFirstMemberPositionProvider() {
    return token.raw;
  }

  @Override
  public InputView getLastMemberPositionProvider() {
    return token.raw;
  }

  @Override
  public String toExpression() {
    return parenthesise(token.raw.buildString());
  }
}
