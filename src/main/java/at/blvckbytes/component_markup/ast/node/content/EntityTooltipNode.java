package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityTooltipNode extends ContentNode {

  public final AExpression type;
  public final AExpression id;
  public final @Nullable AstNode name;

  public EntityTooltipNode(
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
      indent(indentLevel) + "EntityTooltipNode{\n" +
      indent(indentLevel + 1) + "type=" + type.expressionify() + ",\n" +
      indent(indentLevel + 1) + "id=" + id.expressionify() + ",\n" +
      stringifySubtree(name, "name", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
