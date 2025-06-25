package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreNode extends ContentNode {

  public final AExpression name;
  public final AExpression objective;
  public final @Nullable AExpression value;

  public ScoreNode(
    AExpression name,
    AExpression objective,
    @Nullable AExpression value,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.name = name;
    this.objective = objective;
    this.value = value;
  }
}
