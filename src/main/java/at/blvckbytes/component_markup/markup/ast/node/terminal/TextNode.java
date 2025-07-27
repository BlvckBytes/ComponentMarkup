package at.blvckbytes.component_markup.markup.ast.node.terminal;

public class TextNode extends TerminalNode {

  public final String text;

  public TextNode(String text, int position) {
    super(position, null);

    this.text = text;
  }
}
