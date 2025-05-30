package at.blvckbytes.component_markup.xml;

public class XmlParseException extends RuntimeException {

  public final ParseError error;

  public XmlParseException(ParseError error) {
    this.error = error;
  }
}
