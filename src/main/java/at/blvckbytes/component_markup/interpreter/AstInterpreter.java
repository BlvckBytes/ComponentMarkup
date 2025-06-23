package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.control.*;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AstInterpreter implements Interpreter {

  private final IExpressionEvaluator expressionEvaluator;
  private final TemporaryMemberEnvironment environment;
  private final OutputBuilder builder;
  private final InterceptorStack interceptors;

  private AstInterpreter(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment baseEnvironment,
    char breakChar
  ) {
    this.expressionEvaluator = expressionEvaluator;
    this.environment = new TemporaryMemberEnvironment(baseEnvironment);
    this.builder = new OutputBuilder(componentConstructor, environment, breakChar);
    this.interceptors = new InterceptorStack(builder, this);
  }

  public static Object interpretSingle(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment baseEnvironment,
    char breakChar,
    AstNode node
  ) {
    if (breakChar == 0)
      throw new IllegalStateException("Break-char cannot be zero");

    AstInterpreter interpreter = new AstInterpreter(componentConstructor, expressionEvaluator, baseEnvironment, breakChar);
    interpreter.interpret(node);
    return interpreter.builder.getResult().get(0);
  }

  public static List<Object> interpretMulti(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment baseEnvironment,
    AstNode node
  ) {
    AstInterpreter interpreter = new AstInterpreter(componentConstructor, expressionEvaluator, baseEnvironment, (char) 0);
    interpreter.interpret(node);
    return interpreter.builder.getResult();
  }

  @Override
  public List<Object> interpret(AstNode node) {
    _interpret(node);
    return builder.getResult();
  }

  @Override
  public Object joinComponents(List<Object> components) {
    throw new UnsupportedOperationException();
  }

  // TODO: Catch errors while evaluating and return sane defaults (while also logging!)

  @Override
  public String evaluateAsString(AExpression expression) {
    Object value = expressionEvaluator.evaluateExpression(expression, environment);
    return environment.getValueInterpreter().asString(value);
  }

  @Override
  public long evaluateAsLong(AExpression expression) {
    Object value = expressionEvaluator.evaluateExpression(expression, environment);
    return environment.getValueInterpreter().asLong(value);
  }

  @Override
  public double evaluateAsDouble(AExpression expression) {
    Object value = expressionEvaluator.evaluateExpression(expression, environment);
    return environment.getValueInterpreter().asDouble(value);
  }

  @Override
  public @Nullable Boolean evaluateAsBoolean(AExpression expression) {
    Object value = expressionEvaluator.evaluateExpression(expression, environment);

    if (value == null)
      return null;

    return environment.getValueInterpreter().asBoolean(value);
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
