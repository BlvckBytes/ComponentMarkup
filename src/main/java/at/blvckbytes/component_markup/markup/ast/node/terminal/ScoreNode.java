package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreNode extends TerminalNode {

  public final ExpressionNode name;
  public final ExpressionNode objective;
  public final @Nullable ExpressionNode value;

  public ScoreNode(
    ExpressionNode name,
    ExpressionNode objective,
    @Nullable ExpressionNode value,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.name = name;
    this.objective = objective;
    this.value = value;
  }
}
