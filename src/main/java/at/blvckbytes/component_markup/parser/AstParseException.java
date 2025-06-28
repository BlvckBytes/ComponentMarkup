package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.XmlParseException;

public class AstParseException extends RuntimeException {

  public final CursorPosition position;
  public final AstParseError error;

  public AstParseException(CursorPosition position, AstParseError error) {
    this.position = position;
    this.error = error;
  }

  public AstParseException(CursorPosition position, XmlParseException xmlException) {
    super(xmlException);

    this.position = position;
    this.error = AstParseError.XML_PARSE_ERROR;
  }

  public AstParseException(CursorPosition position, ExpressionParseException expressionParseException) {
    super(expressionParseException);

    this.position = position;
    this.error = AstParseError.EXPRESSION_PARSE_ERROR;
  }

  public AstParseException(CursorPosition position, ExpressionTokenizeException expressionTokenizeException) {
    super(expressionTokenizeException);

    this.position = position;
    this.error = AstParseError.EXPRESSION_TOKENIZE_ERROR;
  }
}
