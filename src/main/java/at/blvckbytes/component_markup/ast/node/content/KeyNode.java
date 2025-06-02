package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KeyNode extends ContentNode {

  public final AExpression key;

  public KeyNode(
    AExpression key,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.key = key;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "KeyNode{\n" +
      indent(indentLevel + 1) + "key=" + key.expressionify() + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
