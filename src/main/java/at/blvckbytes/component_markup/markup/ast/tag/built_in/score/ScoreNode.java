package at.blvckbytes.component_markup.markup.ast.tag.built_in.score;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.interpreter.ComponentConstructor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.SlotContext;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ScoreNode extends DeferredNode<ScoreParameter> {

  public final ExpressionNode name;
  public final ExpressionNode objective;
  public final @Nullable ExpressionNode value;

  public ScoreNode(
    ExpressionNode name,
    ExpressionNode objective,
    @Nullable ExpressionNode value,
    CursorPosition position,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.name = name;
    this.objective = objective;
    this.value = value;
  }

  @Override
  public @Nullable List<Object> renderComponent(
    ScoreParameter scoreParameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable Object recipient
  ) {
    // TODO: Implement
    throw new UnsupportedOperationException();
  }

  @Override
  public ScoreParameter createParameter(Interpreter interpreter) {
    String name = interpreter.evaluateAsString(this.name);
    String objective = interpreter.evaluateAsString(this.objective);
    String value = interpreter.evaluateAsStringOrNull(this.value);
    return new ScoreParameter(name, objective, value);
  }
}
