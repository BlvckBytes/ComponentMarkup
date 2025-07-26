package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.util.StringPosition;

public class XmlParseException extends RuntimeException implements ErrorMessage {

  public final XmlParseError error;
  public final StringPosition position;

  public XmlParseException(XmlParseError error, StringPosition position) {
    this.error = error;
    this.position = position;
  }

  @Override
  public String getErrorMessage() {
    return error.getErrorMessage();
  }
}
