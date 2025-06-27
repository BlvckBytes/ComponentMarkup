package at.blvckbytes.component_markup.ast.node.control;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForLoopNode extends AstNode {

  public final ExpressionNode iterable;
  public final String iterationVariable;
  public final AstNode body;
  public final @Nullable AstNode separator;
  public final @Nullable ExpressionNode reversed;

  public ForLoopNode(
    ExpressionNode iterable,
    String iterationVariable,
    AstNode body,
    @Nullable AstNode separator,
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
