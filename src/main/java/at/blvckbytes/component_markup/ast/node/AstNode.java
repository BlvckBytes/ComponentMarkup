package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.xml.CursorPosition;

public abstract class AstNode {

  public final CursorPosition position;

  protected AstNode(CursorPosition position) {
    this.position = position;
  }
}
