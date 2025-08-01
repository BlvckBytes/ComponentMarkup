package at.blvckbytes.component_markup.markup.ast.node.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ItemHoverNode extends HoverNode {

  public final @Nullable ExpressionNode material;
  public final @Nullable ExpressionNode amount;
  public final @Nullable MarkupNode name;
  public final @Nullable MarkupNode lore;
  public final @Nullable ExpressionNode hideProperties;

  public ItemHoverNode(
    @Nullable ExpressionNode material,
    @Nullable ExpressionNode amount,
    @Nullable MarkupNode name,
    @Nullable MarkupNode lore,
    @Nullable ExpressionNode hideProperties,
    StringView positionProvider,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, children, letBindings);

    this.material = material;
    this.amount = amount;
    this.name = name;
    this.lore = lore;
    this.hideProperties = hideProperties;
  }
}
