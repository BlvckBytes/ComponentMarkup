package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.*;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
      .interpret(node, componentConstructor.getSlotContext(slot));
  }

  @Override
  public TemporaryMemberEnvironment getEnvironment() {
    return environment;
  }

  @Override
  public @NotNull String evaluateAsString(ExpressionNode expression) {
    String value = evaluateAsStringOrNull(expression);

    if (value == null)
      return "";

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
      return 0L;

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
      return 0D;

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
      return false;

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
  public List<Object> interpret(MarkupNode node, SlotContext slotContext) {
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

    Map<String, Object> boundVariables = new HashMap<>();

    for (LetBinding letBinding : node.letBindings) {
      Object value = ExpressionInterpreter.interpret(letBinding.expression, environment);

      if (boundVariables.put(letBinding.name, value) != null)
        LoggerProvider.get().log(Level.WARNING, "Duplicate let-binding " + letBinding.name);
    }

    // Set them after evaluating all bindings, such that bindings cannot access each others
    environment.pushVariables(boundVariables);

    return boundVariables.keySet();
  }

  private void interpretForLoop(ForLoopNode node) {
    Object iterable = ExpressionInterpreter.interpret(node.iterable, environment);
    List<Object> items = environment.getValueInterpreter().asList(iterable);

    if (node.iterationVariable != null)
      environment.pushVariable(node.iterationVariable, null);

    LoopVariable loopVariable = new LoopVariable(items.size());
    environment.pushVariable("loop", loopVariable);

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
    if (!doNotUse && node instanceof InterpreterInterceptor)
      interceptors.add((InterpreterInterceptor) node);

    if (node instanceof IfElseIfElseNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      interpretIfElseIfElse((IfElseIfElseNode) node, environment);

      interceptors.handleAfter(node);
      return;
    }

    if (node instanceof WhenMatchingNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      interpretWhenMatching((WhenMatchingNode) node, environment);

      interceptors.handleAfter(node);
      return;
    }

    if (node instanceof ForLoopNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      interpretForLoop((ForLoopNode) node);

      interceptors.handleAfter(node);
      return;
    }

    if (node.ifCondition != null) {
      Object result = ExpressionInterpreter.interpret(node.ifCondition, environment);

      if (!environment.getValueInterpreter().asBoolean(result)) {
        interceptors.handleAfter(node);
        return;
      }
    }

    OutputBuilder builder = getCurrentBuilder();

    if (node instanceof BreakNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      builder.onBreak();

      interceptors.handleAfter(node);
      return;
    }

    if (node instanceof InterpolationNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      ExpressionNode contents = ((InterpolationNode) node).contents;
      Object interpolationValue = evaluateAsPlainObject(contents);

      if (interpolationValue instanceof MarkupNode)
        _interpret((MarkupNode) interpolationValue);
      else {
        String textValue = environment.getValueInterpreter().asString(interpolationValue);
        _interpret(new TextNode(textValue, node.position));
      }

      interceptors.handleAfter(node);
      return;
    }

    Set<String> introducedBindings = introduceLetBindings(node);

    // Terminal nodes always render, because since they do not bear any child-nodes,
    // the only sensible way to "toggle" them is via an if-condition
    if (node instanceof TerminalNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node)) {
        if (introducedBindings != null)
          environment.popVariables(introducedBindings);

        return;
      }

      builder.onTerminal((TerminalNode) node, DelayedCreationHandler.NONE_SENTINEL);

      if (introducedBindings != null)
        environment.popVariables(introducedBindings);

      interceptors.handleAfter(node);
      return;
    }

    if (!doNotUse && interceptors.handleBeforeAndGetIfSkip(node)) {
      if (introducedBindings != null)
        environment.popVariables(introducedBindings);

      return;
    }

    if (node.children != null && !node.children.isEmpty()) {
      builder.onNonTerminalBegin(node);

      for (MarkupNode child : node.children)
        _interpret(child);

      builder.onNonTerminalEnd();
    }

    if (introducedBindings != null)
      environment.popVariables(introducedBindings);

    if (!doNotUse)
      interceptors.handleAfter(node);
  }
}
