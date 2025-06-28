package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.markup.ast.node.control.BreakNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ConditionalNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MarkupInterpreter implements Interpreter {

  private final ComponentConstructor componentConstructor;
  private final ExpressionInterpreter expressionInterpreter;
  private final TemporaryMemberEnvironment environment;
  private final Logger logger;
  private final InterceptorStack interceptors;
  private final Stack<OutputBuilder> builderStack;

  private MarkupInterpreter(
    ComponentConstructor componentConstructor,
    ExpressionInterpreter expressionInterpreter,
    InterpretationEnvironment baseEnvironment,
    Logger logger
  ) {
    this.componentConstructor = componentConstructor;
    this.expressionInterpreter = expressionInterpreter;
    this.environment = new TemporaryMemberEnvironment(baseEnvironment);
    this.logger = logger;
    this.interceptors = new InterceptorStack(this);
    this.builderStack = new Stack<>();
  }

  public static List<Object> interpret(
    ComponentConstructor componentConstructor,
    ExpressionInterpreter expressionInterpreter,
    InterpretationEnvironment baseEnvironment,
    Logger logger,
    char breakChar,
    MarkupNode node
  ) {
    return new MarkupInterpreter(componentConstructor, expressionInterpreter, baseEnvironment, logger)
      .interpret(node, breakChar);
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
      Object result = expressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asString(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a string", e);
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
      Object result = expressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asLong(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a long", e);
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
      Object result = expressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asDouble(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a double", e);
      return null;
    }
  }

  @Override
  public boolean evaluateAsBoolean(ExpressionNode expression) {
    Boolean value = evaluateAsBooleanOrNull(expression);

    if (value == null)
      return false;

    return value;
  }

  @Override
  public @Nullable Boolean evaluateAsBooleanOrNull(ExpressionNode expression) {
    try {
      Object result = expressionInterpreter.interpret(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asBoolean(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a boolean", e);
      return null;
    }
  }

  @Override
  public List<Object> interpret(MarkupNode node, char breakChar) {
    builderStack.push(new OutputBuilder(componentConstructor, this, breakChar));
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

  private void interpretIfElseIfElse(IfElseIfElseNode node, TemporaryMemberEnvironment environment) {
    if (node.conditions.isEmpty())
      throw new IllegalStateException("Expecting at least one condition!");

    for (ConditionalNode conditional : node.conditions) {
      Object result = expressionInterpreter.interpret(conditional.condition, environment);

      if (!environment.getValueInterpreter().asBoolean(result))
        continue;

      _interpret(conditional.body);
      return;
    }

    if (node.fallback == null)
      return;

    _interpret(node.fallback);
  }

  private void interpretConditional(ConditionalNode node) {
    Object result = expressionInterpreter.interpret(node.condition, environment);

    if (environment.getValueInterpreter().asBoolean(result))
      _interpret(node.body);
  }

  private @Nullable Set<String> introduceLetBindings(MarkupNode node) {
    if (node.letBindings == null)
      return null;

    Map<String, Object> boundVariables = new HashMap<>();

    for (LetBinding letBinding : node.letBindings) {
      Object value = expressionInterpreter.interpret(letBinding.expression, environment);

      if (boundVariables.put(letBinding.name, value) != null)
        throw new IllegalStateException("Duplicate let-binding " + letBinding.name);
    }

    // Set them after evaluating all bindings, such that bindings cannot access each others
    environment.pushVariables(boundVariables);

    return boundVariables.keySet();
  }

  private void interpretForLoop(ForLoopNode node) {
    Object iterable = expressionInterpreter.interpret(node.iterable, environment);
    List<Object> items = environment.getValueInterpreter().asList(iterable);

    environment.pushVariable(node.iterationVariable, null);

    LoopVariable loopVariable = new LoopVariable(items.size());
    environment.pushVariable("loop", loopVariable);

    Set<String> introducedNames = introduceLetBindings(node);

    boolean reversed;

    if (node.reversed == null)
      reversed = false;
    else {
      Object reversedValue = expressionInterpreter.interpret(node.reversed, environment);
      reversed = environment.getValueInterpreter().asBoolean(reversedValue);
    }

    int size = items.size();

    for (int index = (reversed ? size - 1 : 0); (reversed ? index >= 0 : index < size); index += (reversed ? -1 : 1)) {
      Object item = items.get(index);

      loopVariable.setIndex(index);

      environment.updateVariable(node.iterationVariable, item);

      if (node.separator != null) {
        if (reversed ? index != size - 1 : index != 0)
          _interpret(node.separator);
      }

      _interpret(node.body);
    }

    if (introducedNames != null)
      environment.popVariables(introducedNames);

    environment.popVariable(node.iterationVariable);
    environment.popVariable("loop");
  }

  private void _interpret(MarkupNode node) {
    if (node instanceof InterpreterInterceptor)
      interceptors.add((InterpreterInterceptor) node);

    if (node instanceof IfElseIfElseNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      interpretIfElseIfElse((IfElseIfElseNode) node, environment);

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

    if (node instanceof ConditionalNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      interpretConditional((ConditionalNode) node);

      interceptors.handleAfter(node);
      return;
    }

    OutputBuilder builder = getCurrentBuilder();

    if (node instanceof BreakNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      builder.onBreak();

      interceptors.handleAfter(node);
      return;
    }

    Set<String> introducedBindings = introduceLetBindings(node);

    if (interceptors.handleBeforeAndGetIfSkip(node)) {
      if (introducedBindings != null)
        environment.popVariables(introducedBindings);

      return;
    }

    if (node instanceof ContentNode) {
      builder.onContent((ContentNode) node);

      if (introducedBindings != null)
        environment.popVariables(introducedBindings);

      interceptors.handleAfter(node);
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

    interceptors.handleAfter(node);
  }
}
