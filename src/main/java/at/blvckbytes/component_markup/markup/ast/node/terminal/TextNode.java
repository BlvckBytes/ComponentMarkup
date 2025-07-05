package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class TextNode extends TerminalNode {

  public final String text;

  public TextNode(String text, CursorPosition position) {
    super(position, null);

    this.text = text;
  }
}
