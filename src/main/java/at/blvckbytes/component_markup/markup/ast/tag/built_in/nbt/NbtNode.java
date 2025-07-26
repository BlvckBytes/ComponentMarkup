package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.interpreter.ComponentConstructor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.SlotContext;
import at.blvckbytes.component_markup.markup.interpreter.SlotType;
import at.blvckbytes.component_markup.util.StringPosition;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class NbtNode extends DeferredNode<NbtParameter> {

  public final NbtSource source;
  public final ExpressionNode identifier;
  public final ExpressionNode path;
  public final @Nullable ExpressionNode interpret;
  public final @Nullable MarkupNode separator;

  public NbtNode(
    NbtSource source,
    ExpressionNode identifier,
    ExpressionNode path,
    @Nullable ExpressionNode interpret,
    @Nullable MarkupNode separator,
    StringPosition position,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.source = source;
    this.identifier = identifier;
    this.path = path;
    this.interpret = interpret;
    this.separator = separator;
  }

  @Override
  public @Nullable List<Object> renderComponent(
    NbtParameter nbtParameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable Object recipient
  ) {
    // TODO: Implement
    throw new UnsupportedOperationException();
  }

  @Override
  public NbtParameter createParameter(Interpreter interpreter) {
    String identifier = interpreter.evaluateAsString(this.identifier);
    String path = interpreter.evaluateAsString(this.path);

    boolean interpret = false;

    if (this.interpret != null)
      interpret = interpreter.evaluateAsBoolean(this.interpret);

    Object separator = null;

    if (this.separator != null) {
      List<Object> components = interpreter.interpretSubtree(
        this.separator,
        interpreter.getComponentConstructor().getSlotContext(SlotType.SINGLE_LINE_CHAT)
      ).unprocessedComponents;

      separator = components.isEmpty() ? null : components.get(0);
    }

    return new NbtParameter(this.source, identifier, path, interpret, separator);
  }
}
