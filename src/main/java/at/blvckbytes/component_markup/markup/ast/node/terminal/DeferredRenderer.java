package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.interpreter.ComponentConstructor;
import at.blvckbytes.component_markup.markup.interpreter.SlotContext;
import org.jetbrains.annotations.Nullable;

public interface DeferredRenderer<Parameter extends RendererParameter> {

  @Nullable Object renderComponent(
    Parameter parameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable Object recipient
  );
}
