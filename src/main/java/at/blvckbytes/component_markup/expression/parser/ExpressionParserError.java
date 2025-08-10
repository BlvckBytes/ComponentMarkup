/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import java.util.function.Function;

public enum ExpressionParserError {
  EXPECTED_EOS(args -> "Expected the input be over at this point (dangling/trailing tokens?)"),
  EXPECTED_SUBSTRING_CLOSING_BRACKET(args -> "Expected a closing-bracket after the substring-invocation: ]"),
  EXPECTED_SUBSCRIPT_CLOSING_BRACKET(args -> "Expected a closing-bracket after the indexing-invocation: ]"),
  EXPECTED_SUBSTRING_UPPER_BOUND(args -> "Expected the substring's upper-bound or a closing-bracket ] as to leave it empty"),
  EXPECTED_RIGHT_INFIX_OPERAND(args -> "Expected a right-hand-side for this infix-operator: " + args[0]),
  EXPECTED_PREFIX_OPERAND(args -> "Expected a right-hand-side for this operator: " + args[0]),
  EXPECTED_PREFIX_OPERAND_CLOSING_PARENTHESIS(args -> "Missing the corresponding closing-parenthesis for this operator: " + args[0]),
  EXPECTED_FALSE_BRANCH(args -> "Expected an expression representing the false branch"),
  EXPECTED_ARRAY_ITEM(args -> "Expected a subsequent array-item"),
  EXPECTED_ARRAY_CLOSING_BRACKET(args -> "Expected a closing-bracket after specifying an array: ]"),
  EXPECTED_PARENTHESES_CONTENT(args -> "Expected the parentheses to bear some content: (...)"),
  EXPECTED_CLOSING_PARENTHESIS(args -> "Expected a closing-parenthesis after opening one: (...)"),
  EXPECTED_MEMBER_ACCESS_IDENTIFIER_RHS(args -> "The right-hand-side of a member-access operation may only be an identifier: " + args[0] + ".<identifier>"),
  ;

  public final Function<String[], String> messageBuilder;

  ExpressionParserError(Function<String[], String> messageBuilder) {
    this.messageBuilder = messageBuilder;
  }
}
