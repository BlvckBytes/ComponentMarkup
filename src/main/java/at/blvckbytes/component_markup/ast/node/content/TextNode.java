package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;

public class TextNode extends ContentNode {

  public final AExpression text;

  public TextNode(
    AExpression text,
    CursorPosition position
  ) {
    super(position, null);

    this.text = text;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "TextNode{\n" +
      indent(indentLevel + 1) + "text=" + text.expressionify() + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
