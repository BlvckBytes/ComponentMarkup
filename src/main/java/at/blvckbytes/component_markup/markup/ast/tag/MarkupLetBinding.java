package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class MarkupLetBinding extends LetBinding {

  public final MarkupNode markup;
  public final boolean capture;

  public MarkupLetBinding(
    MarkupNode markup,
    String name,
    boolean capture,
    CursorPosition position
  ) {
    super(name, position);

    this.markup = markup;
    this.capture = capture;
  }
}
