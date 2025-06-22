package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.xml.CursorPosition;

public class AstParseException extends RuntimeException {

  public final CursorPosition position;
  public final AstParseError error;

  public AstParseException(CursorPosition position, AstParseError error) {
    this.position = position;
    this.error = error;
  }
}
