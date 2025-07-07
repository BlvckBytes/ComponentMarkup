package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.ErrorMessage;

public enum XmlParseError implements ErrorMessage {
  UNTERMINATED_INTERPOLATION("This placeholder misses its closing-bracket: }"),
  UNTERMINATED_STRING("This string misses its closing-quotes: \""),
  UNTERMINATED_MARKUP_VALUE("This markup-value misses its closing-bracket: }"),
  UNTERMINATED_TAG("This tag misses its closing-bracket: >"),
  UNESCAPED_CURLY("Literal curly-brackets need to be escaped: \\{ or \\}"),
  MALFORMED_NUMBER("This number is malformed"),
  MALFORMED_LITERAL_TRUE("This true-literal is malformed"),
  MALFORMED_LITERAL_FALSE("This false-literal is malformed"),
  UNSUPPORTED_ATTRIBUTE_VALUE("This value is not one of: string, number, boolean, markup"),
  MISSING_TAG_NAME("This tag is missing its name"),
  EXPECTED_ATTRIBUTE_KEY("Expected an attribute-key but found a value");

  private final String message;

  XmlParseError(String message) {
    this.message = message;
  }

  @Override
  public String getErrorMessage() {
    return this.message;
  }
}
