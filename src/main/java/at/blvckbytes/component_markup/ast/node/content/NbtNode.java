package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.built_in.nbt.NbtSource;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NbtNode extends ContentNode {

  public final NbtSource source;
  public final AExpression identifier;
  public final AExpression path;
  public final @Nullable AExpression interpret;
  public final @Nullable AstNode separator;

  public NbtNode(
    NbtSource source,
    AExpression identifier,
    AExpression path,
    @Nullable AExpression interpret,
    @Nullable AstNode separator,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.source = source;
    this.identifier = identifier;
    this.path = path;
    this.interpret = interpret;
    this.separator = separator;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "NbtNode{\n" +
      indent(indentLevel + 1) + "source=" + source.name() + ",\n" +
      indent(indentLevel + 1) + "identifier=" + identifier.expressionify() + ",\n" +
      indent(indentLevel + 1) + "path=" + path.expressionify() + ",\n" +
      indent(indentLevel + 1) + "interpret=" + (interpret == null ? "null" : interpret.expressionify()) + ",\n" +
      stringifySubtree(separator, "separator", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
