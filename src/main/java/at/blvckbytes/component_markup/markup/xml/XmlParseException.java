package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.ErrorMessage;

public class XmlParseException extends RuntimeException implements ErrorMessage {

  public final XmlParseError error;

  public XmlParseException(XmlParseError error) {
    this.error = error;
  }

  @Override
  public String getErrorMessage() {
    return error.getErrorMessage();
  }
}
