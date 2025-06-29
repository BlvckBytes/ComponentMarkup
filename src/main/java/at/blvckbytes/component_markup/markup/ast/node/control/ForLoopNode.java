package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForLoopNode extends MarkupNode {

  public final ExpressionNode iterable;
  public final @Nullable String iterationVariable;
  public final MarkupNode body;
  public final @Nullable MarkupNode separator;
  public final @Nullable ExpressionNode reversed;

  public ForLoopNode(
    ExpressionNode iterable,
    @Nullable String iterationVariable,
    MarkupNode body,
    @Nullable MarkupNode separator,
    @Nullable ExpressionNode reversed,
    List<LetBinding> letBindings
  ) {
    super(body.position, null, letBindings);

    this.iterable = iterable;
    this.iterationVariable = iterationVariable;
    this.body = body;
    this.separator = separator;
    this.reversed = reversed;
  }
}
