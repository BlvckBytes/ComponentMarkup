package at.blvckbytes.component_markup.markup.ast.tag.built_in.player_name;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerNameNode extends DeferredNode<PlayerNameParameter> {

  public final @Nullable ExpressionNode displayName;
  public final @Nullable MarkupNode representation;

  public PlayerNameNode(
    @Nullable ExpressionNode displayName,
    @Nullable MarkupNode representation,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.displayName = displayName;
    this.representation = representation;
  }

  @Override
  public @Nullable Object renderComponent(
    PlayerNameParameter parameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable Object recipient
  ) {
    if (recipient == null)
      return null;

    DeferredDataProvider provider = componentConstructor.getDataProvider();
    String name = parameter.displayName ? provider.getDisplayName(recipient) : provider.getName(recipient);

    if (representation == null)
      return componentConstructor.createTextComponent(name);

    environment = environment.copy().withVariable("player_name", name);

    List<Object> components = MarkupInterpreter.interpret(
      componentConstructor, environment, recipient, slotContext, representation
    ).unprocessedComponents;

    if (components.isEmpty())
      return null;

    // TODO: What if the context supports component[] for multiline text? Should we support that?
    return components.get(0);
  }

  @Override
  public PlayerNameParameter createParameter(Interpreter interpreter) {
    return new PlayerNameParameter(interpreter.evaluateAsBoolean(this.displayName));
  }
}
