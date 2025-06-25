package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemHoverNode extends HoverNode {

  public final AExpression material;
  public final @Nullable AExpression amount;
  public final @Nullable AstNode name;
  public final @Nullable AstNode lore;

  public ItemHoverNode(
    AExpression material,
    @Nullable AExpression amount,
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
