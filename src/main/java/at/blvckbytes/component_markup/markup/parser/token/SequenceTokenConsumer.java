package at.blvckbytes.component_markup.markup.parser.token;

@FunctionalInterface
public interface SequenceTokenConsumer {

  void handle(TokenType type, int beginIndex, String value);

}
