package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityHoverNode extends HoverNode {

  public final ExpressionNode type;
  public final ExpressionNode id;
  public final @Nullable MarkupNode name;

  public EntityHoverNode(
    ExpressionNode type,
    ExpressionNode id,
    @Nullable MarkupNode name,
    CursorPosition position,
    List<MarkupNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.type = type;
    this.id = id;
    this.name = name;
  }
}
