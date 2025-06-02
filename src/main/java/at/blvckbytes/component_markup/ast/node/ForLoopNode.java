package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;

public class ForLoopNode extends ContentNode {

  public final AExpression iterable;
  public final String iterationVariable;
  public final AstNode body;

  public ForLoopNode(
    AExpression iterable,
    String iterationVariable,
    AstNode body,
    List<LetBinding> letBindings
  ) {
    super(body.position, null, letBindings);

    this.body = body;
    this.iterable = iterable;
    this.iterationVariable = iterationVariable;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "ForLoopNode{\n" +
      indent(indentLevel + 1) + "iterable='" + iterable.expressionify() + "',\n" +
      indent(indentLevel + 1) + "iterationVariable='" + iterationVariable + "',\n" +
      stringifySubtree(body, "body", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
