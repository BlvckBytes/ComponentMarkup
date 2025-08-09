/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.InterpolationMember;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class StringToken extends TerminalToken {

  public final List<InterpolationMember> members;

  public StringToken(StringView raw, StringView value) {
    super(raw);

    this.members = Collections.singletonList(value);
  }

  public StringToken(StringView raw, List<InterpolationMember> members) {
    super(raw);

    this.members = members;
  }

  @Override
  public @Nullable Object getPlainValue() {
    Object onlyMember;

    // Strings which are not just mere literals have no plain value
    if (members.size() != 1 || !((onlyMember = members.get(0)) instanceof StringView))
      return null;

    return ((StringView) onlyMember).buildString();
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__STRING;
  }
}
