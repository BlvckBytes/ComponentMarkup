package at.blvckbytes.component_markup.markup.ast.node.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

import java.util.List;

public class TextHoverNode extends HoverNode {

  public final MarkupNode value;

  public TextHoverNode(
    MarkupNode value,
    CursorPosition position,
    List<MarkupNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.value = value;
  }
}
