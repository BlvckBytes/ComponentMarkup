/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.UnitNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.*;
import at.blvckbytes.component_markup.util.JsonifyIgnore;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

public abstract class ColorizeNode extends MarkupNode implements InterpreterInterceptor {

  @JsonifyIgnore
  private final ThreadLocal<@Nullable ColorizeNodeState> threadLocalState = ThreadLocal.withInitial(() -> null);

  @JsonifyIgnore
  private final Function<Interpreter<?, ?>, ColorizeNodeState> stateCreator;

  public final InputView tagName;

  public ColorizeNode(
    InputView tagName,
    Function<Interpreter<?, ?>, ColorizeNodeState> stateCreator,
    InputView positionProvider,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, children, letBindings);

    this.tagName = tagName;
    this.stateCreator = stateCreator;
  }

  private ColorizeNodeState getState(Interpreter<?, ?> interpreter) {
    ColorizeNodeState state;

    if ((state = threadLocalState.get()) != null)
      return state;

    state = stateCreator.apply(interpreter);

    threadLocalState.set(state);
    return state;
  }

  protected abstract void onTextEncounter(TextNode node, ColorizeNodeState state, Interpreter<?, ?> interpreter);

  protected abstract void onUnitEncounter(UnitNode node, ColorizeNodeState state, Interpreter<?, ?> interpreter);

  @Override
  public InterceptionResult interceptInterpretation(MarkupNode node, Interpreter<?, ?> interpreter) {
    ColorizeNodeState state = getState(interpreter);

    if (interpreter.getCurrentSubtreeDepth() > state.initialSubtreeDepth && !state.flags.contains(ColorizeFlag.DEEP))
      return InterceptionResult.DO_PROCESS;

    if (node instanceof ColorizeNode && state.doesTargetNode((ColorizeNode) node)) {
      state.begin();
      return InterceptionResult.DO_PROCESS_AND_CALL_AFTER;
    }

    if (node instanceof StyledNode) {
      NodeStyle nodeStyle = ((StyledNode) node).getStyle();

      if (nodeStyle != null && nodeStyle.reset != null && interpreter.evaluateAsBoolean(nodeStyle.reset))
        return InterceptionResult.DO_PROCESS;

      if (node instanceof TerminalNode) {
        if (nodeStyle != null) {
          if (!state.flags.contains(ColorizeFlag.OVERRIDE_COLORS) && nodeStyle.color != null && interpreter.evaluateAsPlainObject(nodeStyle.color) != null)
            return InterceptionResult.DO_PROCESS;
        }

        if (node instanceof TextNode) {
          onTextEncounter((TextNode) node, state, interpreter);
          return InterceptionResult.DO_NOT_PROCESS;
        }

        if (node instanceof UnitNode) {
          onUnitEncounter((UnitNode) node, state, interpreter);
          return InterceptionResult.DO_NOT_PROCESS;
        }
      }
    }

    return InterceptionResult.DO_PROCESS;
  }

  @Override
  public void afterInterpretation(MarkupNode node, Interpreter<?, ?> interpreter) {
    ColorizeNodeState state = getState(interpreter);

    if (!(node instanceof ColorizeNode && state.doesTargetNode((ColorizeNode) node)))
      return;

    if (state.endAndGetIfStackIsEmpty(interpreter))
      threadLocalState.remove();
  }

  @Override
  public void onSkippedByChild(MarkupNode node, Interpreter<?, ?> interpreter, InterceptionResult priorResult) {
    if (priorResult == InterceptionResult.DO_PROCESS_AND_CALL_AFTER)
      getState(interpreter).discard();
  }
}
