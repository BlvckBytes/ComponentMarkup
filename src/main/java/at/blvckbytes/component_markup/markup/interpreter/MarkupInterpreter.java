package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.ExpressionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.*;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupLetBinding;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

public class MarkupInterpreter implements Interpreter {

  private final ComponentConstructor componentConstructor;
  private final TemporaryMemberEnvironment environment;
  private final InterceptorStack interceptors;
  private final Stack<OutputBuilder> builderStack;

  private MarkupInterpreter(ComponentConstructor componentConstructor, InterpretationEnvironment baseEnvironment) {
    this.componentConstructor = componentConstructor;
    this.environment = new TemporaryMemberEnvironment(baseEnvironment);
    this.interceptors = new InterceptorStack(this);
    this.builderStack = new Stack<>();
  }

  public static List<Object> interpret(
    ComponentConstructor componentConstructor,
    InterpretationEnvironment baseEnvironment,
    SlotType slot, MarkupNode node
  ) {
    return new MarkupInterpreter(componentConstructor, baseEnvironment)
      .interpretSubtree(node, componentConstructor.getSlotContext(slot));
  }

  @Override
  public TemporaryMemberEnvironment getEnvironment() {
    return environment;
  }

  @Override
  public @NotNull String evaluateAsString(ExpressionNode expression) {
    String value = evaluateAsStringOrNull(expression);

    if (value == null)
      return environment.getValueInterpreter().asString(null);

    return value;
  }

  @Override
  public @Nullable String evaluateAsStringOrNull(ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asString(result);
    } catch (Throwable e) {
      LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to interpret an expression as a string", e);
      return null;
    }
  }

  @Override
  public long evaluateAsLong(ExpressionNode expression) {
    Long value = evaluateAsLongOrNull(expression);

    if (value == null)
      return environment.getValueInterpreter().asLong(null);

    return value;
  }

  @Override
  public @Nullable Long evaluateAsLongOrNull(ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asLong(result);
    } catch (Throwable e) {
      LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to interpret an expression as a long", e);
      return null;
    }
  }

  @Override
  public double evaluateAsDouble(ExpressionNode expression) {
    Double value = evaluateAsDoubleOrNull(expression);

    if (value == null)
      return environment.getValueInterpreter().asDouble(null);

    return value;
  }

  @Override
  public @Nullable Double evaluateAsDoubleOrNull(ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asDouble(result);
    } catch (Throwable e) {
      LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to interpret an expression as a double", e);
      return null;
    }
  }

  @Override
  public boolean evaluateAsBoolean(ExpressionNode expression) {
    TriState value = evaluateAsTriState(expression);

    if (value == TriState.NULL)
      return environment.getValueInterpreter().asBoolean(null);

    return value == TriState.TRUE;
  }

  @Override
  public TriState evaluateAsTriState(ExpressionNode expression) {
    try {
      Object result = ExpressionInterpreter.interpret(expression, environment);

      if (result == null)
        return TriState.NULL;

      return environment.getValueInterpreter().asBoolean(result) ? TriState.TRUE : TriState.FALSE;
    } catch (Throwable e) {
      LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to interpret an expression as a boolean", e);
      return null;
    }
  }

  @Override
  public @Nullable Object evaluateAsPlainObject(ExpressionNode expression) {
    try {
      return ExpressionInterpreter.interpret(expression, environment);
    } catch (Throwable e) {
      LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to interpret an expression as a plain object", e);
      return null;
    }
  }

  @Override
  public List<Object> interpretSubtree(MarkupNode node, SlotContext slotContext) {
    builderStack.push(new OutputBuilder(componentConstructor, this, slotContext));
    _interpret(node);
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

  private void interpretWhenMatching(WhenMatchingNode node, TemporaryMemberEnvironment environment) {
    if (node.casesLower.isEmpty())
      LoggerProvider.get().log(Level.WARNING, "Encountered empty " + node.getClass().getSimpleName());

    Object result = ExpressionInterpreter.interpret(node.input, environment);

    if (result != null) {
      String inputLower = environment.getValueInterpreter().asString(result).toLowerCase();
      MarkupNode caseNode = node.casesLower.get(inputLower);

      if (caseNode != null) {
        _interpret(caseNode);
        return;
      }
    }

    if (node.other == null)
      return;

    _interpret(node.other);
  }

  private void interpretIfElseIfElse(IfElseIfElseNode node, TemporaryMemberEnvironment environment) {
    if (node.conditions.isEmpty())
      LoggerProvider.get().log(Level.WARNING, "Encountered empty " + node.getClass().getSimpleName());

    for (MarkupNode conditional : node.conditions) {
      if (conditional.ifCondition == null) {
        _interpret(conditional);
        return;
      }

      Object result = ExpressionInterpreter.interpret(conditional.ifCondition, environment);

      if (!environment.getValueInterpreter().asBoolean(result))
        continue;

      _interpret(conditional);
      return;
    }

    if (node.fallback == null)
      return;

    _interpret(node.fallback);
  }

  private @Nullable Set<String> introduceLetBindings(MarkupNode node) {
    if (node.letBindings == null)
      return null;

    Set<String> introducedNames = new HashSet<>();

    for (LetBinding letBinding : node.letBindings) {
      Object value;

      if (letBinding instanceof ExpressionLetBinding)
        value = ExpressionInterpreter.interpret(((ExpressionLetBinding) letBinding).expression, environment);
      else if (letBinding instanceof MarkupLetBinding)
        value = ((MarkupLetBinding) letBinding).markup;
      else
        continue;

      environment.pushVariable(letBinding.name, value);
      introducedNames.add(letBinding.name);
    }

    return introducedNames;
  }

  private void interpretObjectAsNode(
    ExpressionDrivenNode container,
    @Nullable Object value,
    boolean withinCollection
  ) {
    if (!withinCollection && value == null)
      return;

    if (!(value instanceof MarkupNode)) {
      _interpret(new TextNode(String.valueOf(value), container.position));
      return;
    }

    _interpret((MarkupNode) value);
  }

  private void interpretExpressionDriven(ExpressionDrivenNode node) {
    Object value = ExpressionInterpreter.interpret(node.expression, environment);

    if (value instanceof Collection) {
      for (Object item : (Collection<?>) value)
        interpretObjectAsNode(node, item, true);

      return;
    }

    interpretObjectAsNode(node, value, false);
  }

  private <T> T interpretForLoop(ForLoopNode node, Supplier<T> afterEnvironmentSetup) {
    Object iterable = ExpressionInterpreter.interpret(node.iterable, environment);
    List<Object> items = environment.getValueInterpreter().asList(iterable);

    if (node.iterationVariable != null)
      environment.pushVariable(node.iterationVariable, null);

    LoopVariable loopVariable = new LoopVariable(items.size());
    environment.pushVariable("loop", loopVariable);

    T passthroughValue = afterEnvironmentSetup.get();

    boolean reversed;

    if (node.reversed == null)
      reversed = false;
    else {
      Object reversedValue = ExpressionInterpreter.interpret(node.reversed, environment);
      reversed = environment.getValueInterpreter().asBoolean(reversedValue);
    }

    int size = items.size();

    for (int index = (reversed ? size - 1 : 0); (reversed ? index >= 0 : index < size); index += (reversed ? -1 : 1)) {
      Object item = items.get(index);

      loopVariable.setIndex(index);

      if (node.iterationVariable != null)
        environment.updateVariable(node.iterationVariable, item);

      Set<String> introducedNames = introduceLetBindings(node);

      if (node.separator != null) {
        if (reversed ? index != size - 1 : index != 0)
          _interpret(node.separator);
      }

      _interpret(node.body);

      if (introducedNames != null)
        environment.popVariables(introducedNames);
    }

    if (node.iterationVariable != null)
      environment.popVariable(node.iterationVariable);

    environment.popVariable("loop");

    return passthroughValue;
  }

  private void _interpret(MarkupNode node) {
    boolean doNotUse = false;

    if (node.useCondition != null) {
      Object result = ExpressionInterpreter.interpret(node.useCondition, environment);

      if (!environment.getValueInterpreter().asBoolean(result))
        doNotUse = true;
    }

    // Interceptors are what establish additional behaviour, thus do not invoke all
    // of their call-sites in this method if the current node itself is not to be used
    if (!doNotUse) {
      if (node instanceof InterpreterInterceptor)
        interceptors.add((InterpreterInterceptor) node);

      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;
    }

    Set<String> introducedBindings = __interpret(node);

    if (introducedBindings != null)
      environment.popVariables(introducedBindings);

    if (!doNotUse)
      interceptors.handleAfter(node);
  }

  private Set<String> __interpret(MarkupNode node) {
    if (node.ifCondition != null) {
      Object result = ExpressionInterpreter.interpret(node.ifCondition, environment);

      if (!environment.getValueInterpreter().asBoolean(result))
        return null;
    }

    // The for-loop introduces temporary variables itself, so only introduce bindings after the fact
    // such that they have immediate access to said references; it does not make sense to define a
    // let-binding on the very same node a *for attribute is employed on *and* use said binding as
    // the iterable; thus, bindings are granted access to loop-variables, but the iterable doesn't
    // get access to the let-bindings of this very same node - the only logical order.
    if (node instanceof ForLoopNode)
      return interpretForLoop((ForLoopNode) node, () -> introduceLetBindings(node));

    Set<String> introducedBindings = introduceLetBindings(node);

    if (node instanceof IfElseIfElseNode) {
      interpretIfElseIfElse((IfElseIfElseNode) node, environment);
      return introducedBindings;
    }

    if (node instanceof WhenMatchingNode) {
      interpretWhenMatching((WhenMatchingNode) node, environment);
      return introducedBindings;
    }

    if (node instanceof ExpressionDrivenNode) {
      interpretExpressionDriven((ExpressionDrivenNode) node);
      return introducedBindings;
    }

    OutputBuilder builder = getCurrentBuilder();

    if (node instanceof BreakNode) {
      builder.onBreak((BreakNode) node);
      return introducedBindings;
    }

    if (node instanceof InterpolationNode) {
      ExpressionNode contents = ((InterpolationNode) node).contents;
      Object interpolationValue = evaluateAsPlainObject(contents);

      MarkupNode interpolatedNode;

      if (interpolationValue instanceof MarkupNode)
        interpolatedNode = (MarkupNode) interpolationValue;
      else
        interpolatedNode = new TextNode(environment.getValueInterpreter().asString(interpolationValue), node.position);

      if (!interpolatedNode.canBeUnpackedFromAndIfSoInherit(node)) {
        interpolatedNode = new ContainerNode(interpolatedNode.position, Collections.singletonList(interpolatedNode), null);

        if (!interpolatedNode.canBeUnpackedFromAndIfSoInherit(node))
          LoggerProvider.get().log(Level.WARNING, "Could not inherit from InterpolationNode despite containerizing");
      }

      _interpret(interpolatedNode);

      return introducedBindings;
    }

    // Terminal nodes always render, because since they do not bear any child-nodes,
    // the only sensible way to "toggle" them is via an if-condition
    if (node instanceof TerminalNode) {
      builder.onTerminal((TerminalNode) node, DelayedCreationHandler.NONE_SENTINEL);
      return introducedBindings;
    }

    if (node.children != null && !node.children.isEmpty()) {
      builder.onNonTerminalBegin(node);

      for (MarkupNode child : node.children)
        _interpret(child);

      builder.onNonTerminalEnd();
    }

    return introducedBindings;
  }
}
