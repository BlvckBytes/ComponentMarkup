package at.blvckbytes.component_markup.xml;

public class XmlParseException extends RuntimeException {

  public final XmlParseError error;

  public XmlParseException(XmlParseError error) {
    this.error = error;
  }
}
