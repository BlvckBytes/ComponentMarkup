package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class HoverNode extends AstNode {

  public HoverNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);
  }
}
