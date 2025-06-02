package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemTooltipNode extends ContentNode {

  public final String material;
  public final long amount;
  public final AstNode name;
  public final @Nullable AstNode lore;

  public ItemTooltipNode(
    String material,
    long amount,
    AstNode name,
    @Nullable AstNode lore,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(children, letBindings);

    this.material = material;
    this.amount = amount;
    this.name = name;
    this.lore = lore;
  }
}
