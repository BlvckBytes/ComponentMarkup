package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.util.StringPosition;

public class TextNode extends TerminalNode {

  public final String text;

  public TextNode(String text, StringPosition position) {
    super(position, null);

    this.text = text;
  }
}
