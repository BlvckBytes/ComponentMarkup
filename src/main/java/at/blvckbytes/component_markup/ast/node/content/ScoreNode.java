package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreNode extends ContentNode {

  public final String name;
  public final String object;
  public final @Nullable String value;

  public ScoreNode(
    String name,
    String object,
    @Nullable String value,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.name = name;
    this.object = object;
    this.value = value;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "ScoreNode{\n" +
      indent(indentLevel + 1) + "name='" + name + "',\n" +
      indent(indentLevel + 1) + "object='" + object + "',\n" +
      indent(indentLevel + 1) + "value=" + (value == null ? "null" : "'" + value + "'") + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
