package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectorNode extends ContentNode {

  public final ExpressionNode selector;
  public final @Nullable AstNode separator;

  public SelectorNode(
    ExpressionNode selector,
    @Nullable AstNode separator,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.selector = selector;
    this.separator = separator;
  }
}
