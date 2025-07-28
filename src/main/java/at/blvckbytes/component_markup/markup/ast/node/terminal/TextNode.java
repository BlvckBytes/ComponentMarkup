package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.util.StringView;

public class TextNode extends TerminalNode {

  public final StringView text;
  public final String textValue;

  public TextNode(StringView text, String textValue) {
    super(text.startInclusive, null);

    this.text = text;
    this.textValue = textValue;
  }
}
