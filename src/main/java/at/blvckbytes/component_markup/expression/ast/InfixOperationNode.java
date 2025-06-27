package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import org.jetbrains.annotations.Nullable;

public class InfixOperationNode extends ExpressionNode {

  public final ExpressionNode lhs;
  public final InfixOperatorToken operatorToken;
  public final ExpressionNode rhs;
  public final @Nullable PunctuationToken terminator;

  public InfixOperationNode(
    ExpressionNode lhs,
    InfixOperatorToken operatorToken,
    ExpressionNode rhs,
    @Nullable PunctuationToken terminator
  ) {
    super(lhs.beginIndex, terminator == null ? rhs.endIndex : terminator.endIndex);

    this.lhs = lhs;
    this.operatorToken = operatorToken;
    this.rhs = rhs;
    this.terminator = terminator;
  }
}
