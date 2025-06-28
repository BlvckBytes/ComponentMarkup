package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemHoverNode extends HoverNode {

  public final ExpressionNode material;
  public final @Nullable ExpressionNode amount;
  public final @Nullable MarkupNode name;
  public final @Nullable MarkupNode lore;

  public ItemHoverNode(
    ExpressionNode material,
    @Nullable ExpressionNode amount,
    @Nullable MarkupNode name,
    @Nullable MarkupNode lore,
    CursorPosition position,
    List<MarkupNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.material = material;
    this.amount = amount;
    this.name = name;
    this.lore = lore;
  }
}
