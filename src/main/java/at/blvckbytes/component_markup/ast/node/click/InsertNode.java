package at.blvckbytes.component_markup.ast.node.click;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InsertNode extends AstNode {

  public final ExpressionNode value;

  public InsertNode(
    ExpressionNode value,
    CursorPosition position,
    List<AstNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.value = value;
  }
}
