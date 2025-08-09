/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.InterpolationMember;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class TemplateLiteralToken extends TerminalToken {

  public final List<InterpolationMember> members;

  public TemplateLiteralToken(StringView raw, List<InterpolationMember> members) {
    super(raw);

    this.members = members;
  }

  @Override
  public @Nullable Object getPlainValue() {
    LoggerProvider.log(Level.WARNING, "Tried to get the plain-value of a template-literal token");
    return null;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__STRING;
  }
}
