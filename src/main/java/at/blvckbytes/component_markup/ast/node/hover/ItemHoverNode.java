package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemHoverNode extends HoverNode {

  public final ExpressionNode material;
  public final @Nullable ExpressionNode amount;
  public final @Nullable AstNode name;
  public final @Nullable AstNode lore;

  public ItemHoverNode(
    ExpressionNode material,
    @Nullable ExpressionNode amount,
    @Nullable AstNode name,
    @Nullable AstNode lore,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.material = material;
    this.amount = amount;
    this.name = name;
    this.lore = lore;
  }
}
