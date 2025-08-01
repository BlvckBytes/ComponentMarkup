/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;

import java.util.ArrayList;
import java.util.List;

import static at.blvckbytes.component_markup.expression.parser.ExpressionParserTests.*;

public class MemberAndSubscriptChainGenerator {

  private final TextWithSubViews text;
  private final List<String> identifiers;
  private int anchorIndexOffset;

  private MemberAndSubscriptChainGenerator(
    TextWithSubViews text,
    int anchorIndexOffset,
    List<String> identifiers
  ) {
    this.text = text;
    this.identifiers = identifiers;
    this.anchorIndexOffset = anchorIndexOffset;
  }

  private ExpressionNode make() {
    String rhsIdentifier = identifiers.remove(identifiers.size() - 1);

    ExpressionNode lhs;

    if (identifiers.size() == 1) {
      lhs = terminal(identifiers.remove(0), text.subView(anchorIndexOffset));
      ++anchorIndexOffset;
    }

    else
      lhs = make();

    ExpressionNode rhs = terminal(rhsIdentifier, text.subView(anchorIndexOffset + 1));

    InfixOperatorToken operator;
    Token terminator;

    if (rhsIdentifier.startsWith("s_")) {
      operator = new InfixOperatorToken(text.subView(anchorIndexOffset), InfixOperator.SUBSCRIPTING);
      terminator = token(Punctuation.CLOSING_BRACKET, text.subView(anchorIndexOffset + 2));
      anchorIndexOffset += 3;
    }
    else if (rhsIdentifier.startsWith("m_")) {
      operator = new InfixOperatorToken(text.subView(anchorIndexOffset), InfixOperator.MEMBER);
      terminator = null;
      anchorIndexOffset += 2;
    }
    else
      throw new IllegalStateException("Identifier " + rhsIdentifier + " is required to start with s_ or m_");

    return infix(
      lhs,
      operator,
      rhs,
      terminator
    );
  }

  public static ExpressionNode generate(
    TextWithSubViews text,
    int anchorIndexOffset,
    List<String> identifiers
  ) {
    if (identifiers.size() < 2)
      throw new IllegalStateException("Need more than one identifier to build a chain with");

    return new MemberAndSubscriptChainGenerator(text, anchorIndexOffset, new ArrayList<>(identifiers)).make();
  }
}
