package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;

import java.util.ArrayList;
import java.util.List;

import static at.blvckbytes.component_markup.expression.parser.ExpressionParserTests.*;

public class MemberAndSubscriptChainGenerator {

  private final TextWithAnchors text;
  private final List<String> identifiers;
  private int anchorIndexOffset;

  private MemberAndSubscriptChainGenerator(
    TextWithAnchors text,
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
      lhs = terminal(identifiers.remove(0), text.anchorIndex(anchorIndexOffset));
      ++anchorIndexOffset;
    }

    else
      lhs = make();

    int operatorIndex = text.anchorIndex(anchorIndexOffset);
    ExpressionNode rhs = terminal(rhsIdentifier, text.anchorIndex(anchorIndexOffset + 1));

    InfixOperator operator;
    Token terminator;

    if (rhsIdentifier.startsWith("s_")) {
      operator = InfixOperator.SUBSCRIPTING;
      terminator = token(Punctuation.CLOSING_BRACKET, text.anchorIndex(anchorIndexOffset + 2));
      anchorIndexOffset += 3;
    }
    else if (rhsIdentifier.startsWith("m_")) {
      operator = InfixOperator.MEMBER;
      terminator = null;
      anchorIndexOffset += 2;
    }
    else
      throw new IllegalStateException("Identifier " + rhsIdentifier + " is required to start with s_ or m_");

    return infix(
      lhs,
      operator,
      operatorIndex,
      rhs,
      terminator
    );
  }

  public static ExpressionNode generate(
    TextWithAnchors text,
    int anchorIndexOffset,
    List<String> identifiers
  ) {
    if (identifiers.size() < 2)
      throw new IllegalStateException("Need more than one identifier to build a chain with");

    return new MemberAndSubscriptChainGenerator(text, anchorIndexOffset, new ArrayList<>(identifiers)).make();
  }
}
