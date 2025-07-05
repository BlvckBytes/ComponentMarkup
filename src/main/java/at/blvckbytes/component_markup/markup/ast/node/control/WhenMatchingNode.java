package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class WhenMatchingNode extends MarkupNode {

  public final ExpressionNode input;
  public final Map<String, MarkupNode> casesLower;
  public final @Nullable MarkupNode other;

  public WhenMatchingNode(
    CursorPosition position,
    ExpressionNode input,
    Map<String, MarkupNode> casesLower,
    @Nullable MarkupNode other
  ) {
    super(position, null, null);

    this.input = input;
    this.casesLower = casesLower;
    this.other = other;
  }
}
