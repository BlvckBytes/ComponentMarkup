package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.ErrorMessage;

public class XmlParseException extends RuntimeException implements ErrorMessage {

  public final XmlParseError error;
  public final int position;

  public XmlParseException(XmlParseError error, int position) {
    this.error = error;
    this.position = position;
  }

  @Override
  public String getErrorMessage() {
    return error.getErrorMessage();
  }
}
