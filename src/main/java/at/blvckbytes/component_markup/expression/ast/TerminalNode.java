package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;

public class TerminalNode extends ExpressionNode {

  public final TerminalToken token;

  public TerminalNode(TerminalToken token) {
    super(token.beginIndex);

    this.token = token;
  }
}
