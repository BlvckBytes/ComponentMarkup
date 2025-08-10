/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.ExpressionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.control.*;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.UnitNode;
import at.blvckbytes.component_markup.markup.ast.tag.CaptureLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupLetBinding;
import at.blvckbytes.component_markup.platform.ComponentConstructor;
import at.blvckbytes.component_markup.platform.PlatformEntity;
import at.blvckbytes.component_markup.platform.SlotContext;
import at.blvckbytes.component_markup.platform.SlotType;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class MarkupInterpreter implements Interpreter {

  private final ComponentConstructor componentConstructor;
  private final TemporaryMemberEnvironment environment;
  private final @Nullable PlatformEntity recipient;

  private final InterceptorStack interceptors;
  private final Stack<OutputBuilder> builderStack;
  private final SlotContext resetContext;

  private MarkupInterpreter(
    ComponentConstructor componentConstructor,
    InterpretationEnvironment baseEnvironment,
    @Nullable PlatformEntity recipient
  ) {
    this.componentConstructor = componentConstructor;
    this.environment = new TemporaryMemberEnvironment(baseEnvironment);
    this.recipient = recipient;

    this.interceptors = new InterceptorStack(this);
    this.builderStack = new Stack<>();
    this.resetContext = componentConstructor.getSlotContext(SlotType.CHAT);
  }

  public static ComponentOutput interpret(
    ComponentConstructor componentConstructor,
    InterpretationEnvironment baseEnvironment,
    @Nullable PlatformEntity recipient,
    SlotContext slotContext, MarkupNode node
  ) {
    return new MarkupInterpreter(componentConstructor, baseEnvironment, recipient)
      .interpretSubtree(node, slotContext);
  }

  @Override
  public TemporaryMemberEnvironment getEnvironment() {
    return environment;
  }

  @Override
  public @NotNull String evaluateAsString(@Nullable ExpressionNode expression) {
    String value = evaluateAsStringOrNull(expression);

    if (value == null)
      return environment.getValueInterpreter().asString(null);

    return value;
  }

  @Override
  public @Nullable String evaluateAsStringOrNull(@Nullable ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asString(result);
    } catch (Throwable e) {
      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a string", e);
      return null;
    }
  }

  @Override
  public long evaluateAsLong(@Nullable ExpressionNode expression) {
    Long value = evaluateAsLongOrNull(expression);

    if (value == null)
      return environment.getValueInterpreter().asLong(null);

    return value;
  }

  @Override
  public @Nullable Long evaluateAsLongOrNull(@Nullable ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asLong(result);
    } catch (Throwable e) {
      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a long", e);
      return null;
    }
  }

  @Override
  public double evaluateAsDouble(@Nullable ExpressionNode expression) {
    Double value = evaluateAsDoubleOrNull(expression);

    if (value == null)
      return environment.getValueInterpreter().asDouble(null);

    return value;
  }

  @Override
  public @Nullable Double evaluateAsDoubleOrNull(@Nullable ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asDouble(result);
    } catch (Throwable e) {
      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a double", e);
      return null;
    }
  }

  @Override
  public @NotNull Number evaluateAsLongOrDouble(@Nullable ExpressionNode expression) {
    Number value = evaluateAsLongOrDoubleOrNull(expression);

    if (value == null)
      return environment.getValueInterpreter().asLong(null);

    return value;
  }

  @Override
  public @Nullable Number evaluateAsLongOrDoubleOrNull(@Nullable ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asLongOrDouble(result);
    } catch (Throwable e) {
      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a long or double", e);
      return null;
    }
  }

  @Override
  public boolean evaluateAsBoolean(@Nullable ExpressionNode expression) {
    TriState value = evaluateAsTriState(expression);

    if (value == TriState.NULL)
      return environment.getValueInterpreter().asBoolean(null);

    return value == TriState.TRUE;
  }

  @Override
  public TriState evaluateAsTriState(@Nullable ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return TriState.NULL;

      return environment.getValueInterpreter().asBoolean(result) ? TriState.TRUE : TriState.FALSE;
    } catch (Throwable e) {
      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a boolean", e);
      return null;
    }
  }

  @Override
  public @Nullable Object evaluateAsPlainObject(@Nullable ExpressionNode expression) {
    try {
      return ExpressionInterpreter.interpret(expression, environment);
    } catch (Throwable e) {
      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a plain object", e);
      return null;
    }
  }

  public ComponentOutput interpretSubtree(MarkupNode node, SlotContext slotContext) {
    builderStack.push(new OutputBuilder(recipient, componentConstructor, this, slotContext, resetContext));
    interpret(node);
    return builderStack.pop().build();
  }

  @Override
  public OutputBuilder getCurrentBuilder() {
    return builderStack.peek();
  }

  @Override
  public ComponentConstructor getComponentConstructor() {
    return componentConstructor;
  }

  @Override
  public boolean isInSubtree() {
    return builderStack.size() > 1;
  }

  private void interpretWhenMatching(WhenMatchingNode node) {
    if (node.matchingMap.isEmpty())
      LoggerProvider.log(Level.WARNING, "Encountered empty " + node.getClass().getSimpleName());

    Object result = evaluateAsPlainObject(node.input);

    if (result != null) {
      String inputLower = environment.getValueInterpreter().asString(result).toLowerCase();
      MarkupNode caseNode = node.matchingMap.get(inputLower);

      if (caseNode != null) {
        interpret(caseNode);
        return;
      }
    }

    if (node.other == null)
      return;

    interpret(node.other);
  }

  private void interpretIfElseIfElse(IfElseIfElseNode node) {
    if (node.conditions.isEmpty())
      LoggerProvider.log(Level.WARNING, "Encountered empty " + node.getClass().getSimpleName());

    for (MarkupNode conditional : node.conditions) {
      if (conditional.ifCondition == null) {
        interpret(conditional);
        return;
      }

      if (!evaluateAsBoolean(conditional.ifCondition))
        continue;

      interpret(conditional);
      return;
    }

    if (node.fallback == null)
      return;

    interpret(node.fallback);
  }

  private MarkupNode createVariableCapture(MarkupNode node, LetBinding binding) {
    LinkedHashSet<LetBinding> capturedBindings = new LinkedHashSet<>();

    environment.forEachKnownName(name -> {
      Object variableValue = environment.getVariableValue(name);

      if (variableValue instanceof InternalCopyable)
        variableValue = ((InternalCopyable) variableValue).copy();

      capturedBindings.add(new CaptureLetBinding(variableValue, name, binding.name));
    });

    return new CaptureNode(node, capturedBindings);
  }

  private void introduceLetBindings(@Nullable Collection<LetBinding> letBindings) {
    if (letBindings == null)
      return;

    for (LetBinding letBinding : letBindings) {
      Object value;
      String name;

      if (letBinding instanceof ExpressionLetBinding) {
        ExpressionLetBinding expressionBinding = (ExpressionLetBinding) letBinding;
        value = evaluateAsPlainObject(((ExpressionLetBinding) letBinding).expression);
        name = expressionBinding.bindingName;

        if (expressionBinding.capture) {
          if (!(value instanceof MarkupNode))
            value = new TextNode(letBinding.name, String.valueOf(value));

          value = createVariableCapture((MarkupNode) value, letBinding);
        }
      }
      else if (letBinding instanceof MarkupLetBinding) {
        MarkupLetBinding markupBinding = (MarkupLetBinding) letBinding;
        value = markupBinding.markup;
        name = markupBinding.bindingName;

        if (markupBinding.capture)
          value = createVariableCapture((MarkupNode) value, letBinding);
      }
      else if (letBinding instanceof CaptureLetBinding) {
        CaptureLetBinding captureBinding = (CaptureLetBinding) letBinding;
        value = captureBinding.capturedValue;
        name = captureBinding.capturedName;
      } else {
        LoggerProvider.log(Level.WARNING, "Encountered unknown let-binding type: " + (letBinding == null ? null : letBinding.getClass()));
        continue;
      }

      environment.setScopeVariable(name, value);
    }
  }

  private void interpretObjectAsNode(
    @Nullable Object value,
    boolean withinCollection
  ) {
    if (!withinCollection && value == null)
      return;

    if (!(value instanceof MarkupNode)) {
      interpret(new TextNode(StringView.EMPTY, String.valueOf(value)));
      return;
    }

    interpret((MarkupNode) value);
  }

  private void interpretFunctionDriven(FunctionDrivenNode node) {
    Object value = node.function.apply(this);

    if (value instanceof Collection) {
      for (Object item : (Collection<?>) value)
        interpretObjectAsNode(item, true);

      return;
    }

    interpretObjectAsNode(value, false);
  }

  private void interpretExpressionDriven(ExpressionDrivenNode node) {
    Object value = evaluateAsPlainObject(node.expression);

    if (value instanceof Collection) {
      for (Object item : (Collection<?>) value)
        interpretObjectAsNode(item, true);

      return;
    }

    interpretObjectAsNode(value, false);
  }

  private void interpretForLoop(ForLoopNode node) {
    introduceLetBindings(node.letBindingsBeforeForAttribute);

    Object iterable = evaluateAsPlainObject(node.iterable);
    List<Object> items = environment.getValueInterpreter().asList(iterable);

    LoopVariable loopVariable = new LoopVariable(items.size());
    environment.setScopeVariable("loop", loopVariable);

    int size = items.size();

    if (size == 0) {
      if (node.empty != null) {
        introduceLetBindings(node.letBindingsAfterForAttribute);
        interpret(node.empty);
      }

      return;
    }

    boolean reversed = evaluateAsBoolean(node.reversed);

    for (int index = (reversed ? size - 1 : 0); (reversed ? index >= 0 : index < size); index += (reversed ? -1 : 1)) {
      Object item = items.get(index);

      loopVariable.setIndex(index);

      if (node.iterationVariable != null)
        environment.setScopeVariable(node.iterationVariable, item);

      introduceLetBindings(node.letBindingsAfterForAttribute);

      if (node.separator != null) {
        if (reversed ? index != size - 1 : index != 0)
          interpret(node.separator);
      }

      interpret(node.body);
    }
  }

  @Override
  public void interpret(MarkupNode node) {
    interpret(node, null);
  }

  @Override
  public void interpret(MarkupNode node, @Nullable Runnable afterScopeBegin) {
    boolean doNotUse = false;

    if (node.useCondition != null) {
      if (!evaluateAsBoolean(node.useCondition))
        doNotUse = true;
    }

    // Interceptors are what establish additional behaviour, thus do not invoke all
    // of their call-sites in this method if the current node itself is not to be used
    if (!doNotUse) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;
    }

    environment.beginScope();

    if (afterScopeBegin != null)
      afterScopeBegin.run();

    _interpret(node);

    environment.endScope();

    if (!doNotUse)
      interceptors.handleAfter(node);
  }

  private void _interpret(MarkupNode node) {
    if (node.ifCondition != null && !evaluateAsBoolean(node.ifCondition))
      return;

    // The for-loop introduces temporary variables itself, so only introduce bindings after the fact
    // such that they have immediate access to said references; it does not make sense to define a
    // let-binding on the very same node a *for attribute is employed on *and* use said binding as
    // the iterable; thus, bindings are granted access to loop-variables, but the iterable doesn't
    // get access to the let-bindings of this very same node - the only logical order. Thus, we now
    // hand control regarding the loop's bindings over to the corresponding sub-interpreter.
    if (node instanceof ForLoopNode) {
      interpretForLoop((ForLoopNode) node);
      return;
    }

    introduceLetBindings(node.letBindings);

    if (node instanceof IfElseIfElseNode) {
      interpretIfElseIfElse((IfElseIfElseNode) node);
      return;
    }

    if (node instanceof WhenMatchingNode) {
      interpretWhenMatching((WhenMatchingNode) node);
      return;
    }

    if (node instanceof ExpressionDrivenNode) {
      interpretExpressionDriven((ExpressionDrivenNode) node);
      return;
    }

    if (node instanceof FunctionDrivenNode) {
      interpretFunctionDriven((FunctionDrivenNode) node);
      return;
    }

    OutputBuilder builder = getCurrentBuilder();

    if (node instanceof BreakNode) {
      builder.onBreak();
      return;
    }

    if (node instanceof InterpolationNode) {
      ExpressionNode contents = ((InterpolationNode) node).contents;
      Object interpolationValue = evaluateAsPlainObject(contents);

      if (interpolationValue == null)
        return;

      MarkupNode interpolatedNode;

      if (interpolationValue instanceof MarkupNode)
        interpolatedNode = (MarkupNode) interpolationValue;
      else
        interpolatedNode = new TextNode(StringView.EMPTY, environment.getValueInterpreter().asString(interpolationValue));

      NodeStyle nodeStyle = ((InterpolationNode) node).getStyle();

      if (nodeStyle != null) {
        if (!(interpolatedNode instanceof StyledNode))
          interpolatedNode = new ContainerNode(interpolatedNode.positionProvider, Collections.singletonList(interpolatedNode), null);

        ((StyledNode) interpolatedNode).getOrInstantiateStyle().inheritFrom(nodeStyle, node.useCondition);
      }

      interpret(interpolatedNode);

      return;
    }

    // Terminal nodes always render, because since they do not bear any child-nodes,
    // the only sensible way to "toggle" them is via an if-condition
    if (node instanceof TerminalNode) {
      if (node instanceof UnitNode)
        builder.onUnit((UnitNode) node, null);

      else if (node instanceof TextNode)
        builder.onText((TextNode) node, null, false);

      return;
    }

    if (node.children != null && !node.children.isEmpty()) {
      builder.onNonTerminalBegin(node);

      for (MarkupNode child : node.children)
        interpret(child);

      builder.onNonTerminalEnd();
    }
  }
}
