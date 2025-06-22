package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.control.*;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AstInterpreter {

  private final ComponentConstructor componentConstructor;
  private final IExpressionEvaluator expressionEvaluator;
  private final IEvaluationEnvironment baseEnvironment;
  private final BreakMode breakMode;

  public AstInterpreter(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment environment,
    BreakMode breakMode
  ) {
    this.componentConstructor = componentConstructor;
    this.expressionEvaluator = expressionEvaluator;
    this.baseEnvironment = environment;
    this.breakMode = breakMode;
  }

  public List<Object> interpret(AstNode node) {
    TemporaryMemberEnvironment environment = new TemporaryMemberEnvironment(baseEnvironment);
    OutputBuilder builder = new OutputBuilder(componentConstructor, environment, breakMode);
    _interpret(node, builder, environment);
    return builder.getResult();
  }

  private void interpretIfThenElse(IfThenElseNode node, OutputBuilder builder, TemporaryMemberEnvironment environment) {
    if (node.conditions.isEmpty())
      throw new IllegalStateException("Expecting at least one condition!");

    for (ConditionalNode conditional : node.conditions) {
      Object result = expressionEvaluator.evaluateExpression(conditional.condition, environment);

      if (!environment.getValueInterpreter().asBoolean(result))
        continue;

      _interpret(conditional.body, builder, environment);
      return;
    }

    if (node.fallback == null)
      return;

    _interpret(node.fallback, builder, environment);
  }

  private void interpretConditional(ConditionalNode node, OutputBuilder builder, TemporaryMemberEnvironment environment) {
    Object result = expressionEvaluator.evaluateExpression(node.condition, environment);

    if (environment.getValueInterpreter().asBoolean(result))
      _interpret(node.body, builder, environment);
  }

  private @Nullable Set<String> introduceLetBindings(AstNode node, TemporaryMemberEnvironment environment) {
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

  private void interpretForLoop(ForLoopNode node, OutputBuilder builder, TemporaryMemberEnvironment environment) {
    Object iterable = expressionEvaluator.evaluateExpression(node.iterable, environment);
    List<Object> items = environment.getValueInterpreter().asCollection(iterable);

    environment.pushVariable(node.iterationVariable, null);

    LoopVariable loopVariable = new LoopVariable(items.size());
    environment.pushVariable("loop", loopVariable);

    Set<String> introducedNames = introduceLetBindings(node, environment);

    for (int index = 0; index < items.size(); ++index) {
      Object item = items.get(index);

      loopVariable.setIndex(index);

      environment.updateVariable(node.iterationVariable, item);

      _interpret(node.body, builder, environment);
    }

    if (introducedNames != null)
      environment.popVariables(introducedNames);

    environment.popVariable(node.iterationVariable);
    environment.popVariable("loop");
  }

  private void _interpret(AstNode node, OutputBuilder builder, TemporaryMemberEnvironment environment) {
    if (node instanceof IfThenElseNode) {
      interpretIfThenElse((IfThenElseNode) node, builder, environment);
      return;
    }

    if (node instanceof ForLoopNode) {
      interpretForLoop((ForLoopNode) node, builder, environment);
      return;
    }

    if (node instanceof ConditionalNode) {
      interpretConditional((ConditionalNode) node, builder, environment);
      return;
    }

    if (node instanceof BreakNode) {
      builder.onBreak();
      return;
    }

    Set<String> introducedBindings = introduceLetBindings(node, environment);

    if (node instanceof ContentNode) {
      builder.onContent((ContentNode) node);

      if (introducedBindings != null)
        environment.popVariables(introducedBindings);

      return;
    }

    builder.onNonTerminalBegin(node);

    if (node.children == null || node.children.isEmpty())
      throw new IllegalStateException("Encountered empty non-terminal tag");

    for (AstNode child : node.children)
      _interpret(child, builder, environment);

    builder.onNonTerminalEnd();

    if (introducedBindings != null)
      environment.popVariables(introducedBindings);
  }
}
