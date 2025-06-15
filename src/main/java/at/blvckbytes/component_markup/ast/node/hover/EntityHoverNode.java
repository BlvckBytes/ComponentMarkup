package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityHoverNode extends HoverNode {

  public final AExpression type;
  public final AExpression id;
  public final @Nullable AstNode name;

  public EntityHoverNode(
    AExpression type,
    AExpression id,
    @Nullable AstNode name,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.type = type;
    this.id = id;
    this.name = name;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "EntityHoverNode{\n" +
      indent(indentLevel + 1) + "type=" + type.expressionify() + ",\n" +
      indent(indentLevel + 1) + "id=" + id.expressionify() + ",\n" +
      stringifySubtree(name, "name", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
