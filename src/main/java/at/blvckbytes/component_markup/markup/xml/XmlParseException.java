package at.blvckbytes.component_markup.markup.xml;

public class XmlParseException extends RuntimeException {

  public final XmlParseError error;

  public XmlParseException(XmlParseError error) {
    this.error = error;
  }
}
