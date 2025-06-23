package at.blvckbytes.component_markup.ast.tag.built_in.gradient;

import at.blvckbytes.component_markup.ast.node.content.PendingTextNode;

public class NodeAndChar {
  public final PendingTextNode node;
  public final char character;

  public NodeAndChar(PendingTextNode node, char character) {
    this.node = node;
    this.character = character;
  }
}
