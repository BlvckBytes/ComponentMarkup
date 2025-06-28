package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.ErrorMessage;

public enum ExpressionParserError implements ErrorMessage {
  EXPECTED_EOS("Expected the input be over at this point (dangling/trailing tokens?)"),
  EXPECTED_SUBSTRING_CLOSING_BRACKET("Expected a closing-bracket ] after the substring-invocation"),
  EXPECTED_SUBSCRIPT_CLOSING_BRACKET("Expected a closing-bracket ] after the indexing-invocation"),
  EXPECTED_SUBSTRING_UPPER_BOUND("Expected the substring's upper-bound or a closing-bracket ] as to leave it empty"),
  EXPECTED_RIGHT_INFIX_OPERAND("Expected a right-hand-side for this infix-operator"),
  EXPECTED_PREFIX_OPERAND("Expected a right-hand-side for this prefix-operator"),
  EXPECTED_BRANCH_DELIMITER("Expected the branching-delimiter: :"),
  EXPECTED_FALSE_BRANCH("Expected an expression representing the false branch"),
  EXPECTED_ARRAY_ITEM("Expected a subsequent array-item"),
  EXPECTED_ARRAY_CLOSING_BRACKET("Expected a closing-bracket ] after specifying an array"),
  EXPECTED_PARENTHESES_CONTENT("Expected parentheses to bear some content"),
  EXPECTED_CLOSING_PARENTHESIS("Expected a closing-parenthesis ) after opening one"),
  EXPECTED_MEMBER_ACCESS_IDENTIFIER_RHS("The right-hand-side of a member-access (.) operation may only be an identifier"),
  ;

  private final String message;

  ExpressionParserError(String message) {
    this.message = message;
  }

  @Override
  public String getErrorMessage() {
    return this.message;
  }
}
