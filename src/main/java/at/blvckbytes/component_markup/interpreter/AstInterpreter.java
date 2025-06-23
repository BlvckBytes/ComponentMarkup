package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.control.*;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AstInterpreter implements Interpreter {

  private final ComponentConstructor componentConstructor;
  private final IExpressionEvaluator expressionEvaluator;
  private final TemporaryMemberEnvironment environment;
  private final Logger logger;
  private final InterceptorStack interceptors;
  private final Stack<OutputBuilder> builderStack;

  private AstInterpreter(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment baseEnvironment,
    Logger logger
  ) {
    this.componentConstructor = componentConstructor;
    this.expressionEvaluator = expressionEvaluator;
    this.environment = new TemporaryMemberEnvironment(baseEnvironment);
    this.logger = logger;
    this.interceptors = new InterceptorStack(this);
    this.builderStack = new Stack<>();
  }

  public static Object interpretSingle(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment baseEnvironment,
    Logger logger,
    char breakChar,
    AstNode node
  ) {
    if (breakChar == 0)
      throw new IllegalStateException("Break-char cannot be zero");

    return new AstInterpreter(componentConstructor, expressionEvaluator, baseEnvironment, logger)
      .interpret(node, breakChar).get(0);
  }

  public static List<Object> interpretMulti(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment baseEnvironment,
    Logger logger,
    AstNode node
  ) {
    return new AstInterpreter(componentConstructor, expressionEvaluator, baseEnvironment, logger)
      .interpret(node, (char) 0);
  }

  @Override
  public @NotNull String evaluateAsString(AExpression expression) {
    String value = evaluateAsStringOrNull(expression);

    if (value == null)
      return "";

    return value;
  }

  @Override
  public @Nullable String evaluateAsStringOrNull(AExpression expression) {
    try {
      Object result = expressionEvaluator.evaluateExpression(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asString(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a string", e);
      return null;
    }
  }

  @Override
  public long evaluateAsLong(AExpression expression) {
    Long value = evaluateAsLongOrNull(expression);

    if (value == null)
      return 0L;

    return value;
  }

  @Override
  public @Nullable Long evaluateAsLongOrNull(AExpression expression) {
    try {
      Object result = expressionEvaluator.evaluateExpression(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asLong(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a long", e);
      return null;
    }
  }

  @Override
  public double evaluateAsDouble(AExpression expression) {
    Double value = evaluateAsDoubleOrNull(expression);

    if (value == null)
      return 0D;

    return value;
  }

  @Override
  public @Nullable Double evaluateAsDoubleOrNull(AExpression expression) {
    try {
      Object result = expressionEvaluator.evaluateExpression(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asDouble(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a double", e);
      return null;
    }
  }

  @Override
  public boolean evaluateAsBoolean(AExpression expression) {
    Boolean value = evaluateAsBooleanOrNull(expression);

    if (value == null)
      return false;

    return value;
  }

  @Override
  public @Nullable Boolean evaluateAsBooleanOrNull(AExpression expression) {
    try {
      Object result = expressionEvaluator.evaluateExpression(expression, environment);

      if (result == null)
        return null;

      return environment.getValueInterpreter().asBoolean(result);
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to interpret an expression as a boolean", e);
      return null;
    }
  }

  @Override
  public List<Object> interpret(AstNode node, char breakChar) {
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

  private void interpretIfThenElse(IfThenElseNode node, TemporaryMemberEnvironment environment) {
    if (node.conditions.isEmpty())
      throw new IllegalStateException("Expecting at least one condition!");

    for (ConditionalNode conditional : node.conditions) {
      Object result = expressionEvaluator.evaluateExpression(conditional.condition, environment);

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
    Object result = expressionEvaluator.evaluateExpression(node.condition, environment);

    if (environment.getValueInterpreter().asBoolean(result))
      _interpret(node.body);
  }

  private @Nullable Set<String> introduceLetBindings(AstNode node) {
    if (node.letBindings == null)
      return null;

    Map<String, Object> boundVariables = new HashMap<>();

    for (LetBinding letBinding : node.letBindings) {
      Object value = expressionEvaluator.evaluateExpression(letBinding.expression, environment);

      if (boundVariables.put(letBinding.name, value) != null)
        throw new IllegalStateException("Duplicate let-binding " + letBinding.name);
    }

    // Set them after evaluating all bindings, such that bindings cannot access each others
    environment.pushVariables(boundVariables);

    return boundVariables.keySet();
  }

  private void interpretForLoop(ForLoopNode node) {
    Object iterable = expressionEvaluator.evaluateExpression(node.iterable, environment);
    List<Object> items = environment.getValueInterpreter().asCollection(iterable);

    environment.pushVariable(node.iterationVariable, null);

    LoopVariable loopVariable = new LoopVariable(items.size());
    environment.pushVariable("loop", loopVariable);

    Set<String> introducedNames = introduceLetBindings(node);

    for (int index = 0; index < items.size(); ++index) {
      Object item = items.get(index);

      loopVariable.setIndex(index);

      environment.updateVariable(node.iterationVariable, item);

      _interpret(node.body);
    }

    if (introducedNames != null)
      environment.popVariables(introducedNames);

    environment.popVariable(node.iterationVariable);
    environment.popVariable("loop");
  }

  private void _interpret(AstNode node) {
    if (node instanceof InterpreterInterceptor)
      interceptors.add((InterpreterInterceptor) node);

    if (node instanceof IfThenElseNode) {
      if (interceptors.handleBeforeAndGetIfSkip(node))
        return;

      interpretIfThenElse((IfThenElseNode) node, environment);

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

      for (AstNode child : node.children)
        _interpret(child);

      builder.onNonTerminalEnd();
    }

    if (introducedBindings != null)
      environment.popVariables(introducedBindings);

    interceptors.handleAfter(node);
  }
}
