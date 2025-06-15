package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextNode extends ContentNode {

  public final AExpression text;

  public TextNode(
    AExpression text,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

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
