package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectorNode extends UnitNode {

  public final ExpressionNode selector;
  public final @Nullable MarkupNode separator;

  public SelectorNode(
    ExpressionNode selector,
    @Nullable MarkupNode separator,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.selector = selector;
    this.separator = separator;
  }
}
