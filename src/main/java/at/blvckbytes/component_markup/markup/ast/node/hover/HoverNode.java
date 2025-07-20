package at.blvckbytes.component_markup.markup.ast.node.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class HoverNode extends MarkupNode {

  public HoverNode(
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable Set<LetBinding> letBindings
  ) {
    super(position, children, letBindings);
  }
}
