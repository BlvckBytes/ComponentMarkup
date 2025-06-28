package at.blvckbytes.component_markup.markup.ast.node.click;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InsertNode extends MarkupNode {

  public final ExpressionNode value;

  public InsertNode(
    ExpressionNode value,
    CursorPosition position,
    List<MarkupNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.value = value;
  }
}
