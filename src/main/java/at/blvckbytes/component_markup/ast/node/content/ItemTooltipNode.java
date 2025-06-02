package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemTooltipNode extends ContentNode {

  public final String material;
  public final long amount;
  public final @Nullable AstNode name;
  public final @Nullable AstNode lore;

  public ItemTooltipNode(
    String material,
    long amount,
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

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "ItemTooltipNode{\n" +
      indent(indentLevel + 1) + "material='" + material + "',\n" +
      indent(indentLevel + 1) + "amount=" + amount + ",\n" +
      stringifySubtree(name, "name", indentLevel + 1) + ",\n" +
      stringifySubtree(lore, "lore", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
