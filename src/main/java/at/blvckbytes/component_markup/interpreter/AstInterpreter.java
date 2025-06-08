package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.control.*;
import at.blvckbytes.component_markup.ast.node.tooltip.TooltipNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AstInterpreter {

  private final IExpressionEvaluator expressionEvaluator;
  private final TemporaryMemberEnvironment environment;
  private final OutputBuilder outputBuilder;

  private AstInterpreter(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment environment,
    BreakMode breakMode
  ) {
    this.expressionEvaluator = expressionEvaluator;
    this.environment = new TemporaryMemberEnvironment(environment);
    this.outputBuilder = new OutputBuilder(componentConstructor, environment, breakMode);
  }

  private void interpretIfThenElse(IfThenElseNode node) {
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

  private void interpretForLoop(ForLoopNode node) {
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

      _interpret(node.body);
    }

    if (introducedNames != null)
      environment.popVariables(introducedNames);

    environment.popVariable(node.iterationVariable);
    environment.popVariable("loop");
  }

  private void _interpret(AstNode node) {
    if (node instanceof IfThenElseNode) {
      interpretIfThenElse((IfThenElseNode) node);
      return;
    }

    if (node instanceof ForLoopNode) {
      interpretForLoop((ForLoopNode) node);
      return;
    }

    if (node instanceof BreakNode) {
      outputBuilder.onBreak();
      return;
    }

    if (node instanceof ConditionalNode)
      throw new IllegalStateException("Conditional nodes are only allowed to exist as members of if-then-else nodes");

    Set<String> introducedBindings = introduceLetBindings(node, environment);

    if (node instanceof ContentNode) {
      outputBuilder.onContent((ContentNode) node);

      if (introducedBindings != null)
        environment.popVariables(introducedBindings);

      return;
    }

    if (node instanceof ContainerNode)
      outputBuilder.onContainerBegin((ContainerNode) node);
    else if (node instanceof ClickNode)
      outputBuilder.onClickBegin((ClickNode) node);
    else if (node instanceof InsertNode)
      outputBuilder.onInsertBegin((InsertNode) node);
    else if (node instanceof TooltipNode)
      outputBuilder.onTooltipBegin((TooltipNode) node);
    else
      throw new IllegalStateException("Unknown AST-node: " + node);

    assert node.children != null;

    for (AstNode child : node.children)
      _interpret(child);

    outputBuilder.onAnyEnd();

    if (introducedBindings != null)
      environment.popVariables(introducedBindings);
  }

  public static List<Object> interpret(
    ComponentConstructor componentConstructor,
    IExpressionEvaluator expressionEvaluator,
    IEvaluationEnvironment environment,
    BreakMode breakMode,
    AstNode rootNode
  ) {
    AstInterpreter interpreter = new AstInterpreter(componentConstructor, expressionEvaluator, environment, breakMode);
    interpreter._interpret(rootNode);
    return interpreter.outputBuilder.getResult();
  }
}
