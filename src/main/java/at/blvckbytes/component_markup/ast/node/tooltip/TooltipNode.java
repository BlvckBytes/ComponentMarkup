package at.blvckbytes.component_markup.ast.node.tooltip;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class TooltipNode extends AstNode {

  public TooltipNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);
  }
}
