package at.blvckbytes.component_markup.ast.node.control;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForLoopNode extends AstNode {

  public final AExpression iterable;
  public final String iterationVariable;
  public final AstNode body;
  public final @Nullable AstNode separator;

  public ForLoopNode(
    AExpression iterable,
    String iterationVariable,
    AstNode body,
    @Nullable AstNode separator,
    List<LetBinding> letBindings
  ) {
    super(body.position, null, letBindings);

    this.iterable = iterable;
    this.iterationVariable = iterationVariable;
    this.body = body;
    this.separator = separator;
  }
}
