package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class TextTooltipNode extends AstNode {

  public final AstNode value;

  public TextTooltipNode(
    AstNode value,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.value = value;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "TextTooltipNode{\n" +
      stringifySubtree(value, "value", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
