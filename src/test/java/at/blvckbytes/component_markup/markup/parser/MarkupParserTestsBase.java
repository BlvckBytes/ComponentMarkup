package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.ExpressionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.WhenMatchingNode;
import at.blvckbytes.component_markup.markup.ast.node.control.InterpolationNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TranslateNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupList;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public abstract class MarkupParserTestsBase {

  protected static CursorPosition zeroCursor(TextWithAnchors input) {
    return new CursorPosition(0, 0, 0, input.text);
  }

  protected static NodeWrapper<ForLoopNode> forLoop(ExpressionNode iterable, @Nullable String iterationVariable, NodeWrapper<?> wrappedBody, @Nullable NodeWrapper<?> wrappedSeparator, @Nullable ExpressionNode reversed, @Nullable NodeWrapper<?> wrappedEmpty) {
    return new NodeWrapper<>(new ForLoopNode(iterable, iterationVariable, wrappedBody.get(), wrappedSeparator == null ? null : wrappedSeparator.get(), wrappedEmpty == null ? null : wrappedEmpty.get(), reversed, new LinkedHashSet<>()));
  }

  protected static Map<String, NodeWrapper<? extends MarkupNode>> whenMap(Object... items) {
    Map<String, NodeWrapper<? extends MarkupNode>> result = new HashMap<>();

    if (items.length % 2 != 0)
      throw new IllegalStateException("Expected an even number of items");

    for (int i = 0; i < items.length; i += 2) {
      String key = (String) items[i];
      NodeWrapper<?> value = (NodeWrapper<?>) items[i + 1];
      result.put(key, value);
    }

    return result;
  }

  protected static NodeWrapper<WhenMatchingNode> when(CursorPosition position, ExpressionNode input, @Nullable NodeWrapper<?> wrappedFallback, Map<String, NodeWrapper<? extends MarkupNode>> wrappedCases) {
    Map<String, MarkupNode> cases = new HashMap<>();

    for (Map.Entry<String, NodeWrapper<? extends MarkupNode>> entry : wrappedCases.entrySet())
      cases.put(entry.getKey().toLowerCase(), entry.getValue().get());

    return new NodeWrapper<>(new WhenMatchingNode(position, input, cases, wrappedFallback == null ? null : wrappedFallback.get()));
  }

  @SafeVarargs
  protected static NodeWrapper<IfElseIfElseNode> ifElseIfElse(@Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<? extends MarkupNode>... wrappedConditions) {
    List<MarkupNode> conditions = new ArrayList<>();

    for (NodeWrapper<? extends MarkupNode> wrappedCondition : wrappedConditions)
      conditions.add(wrappedCondition.get());

    return new NodeWrapper<>(new IfElseIfElseNode(conditions, wrappedFallback == null ? null : wrappedFallback.get()));
  }

  protected static ExpressionNode imm(String value) {
    return ImmediateExpression.of(value);
  }

  protected static ExpressionNode imm(boolean value) {
    return ImmediateExpression.of(value);
  }

  protected static ExpressionNode expr(String expression) {
    return ExpressionParser.parse(expression);
  }

  protected static NodeWrapper<ContainerNode> container(CursorPosition position) {
    return new NodeWrapper<>(new ContainerNode(position, new ArrayList<>(), new LinkedHashSet<>()));
  }

  protected static NodeWrapper<ExpressionDrivenNode> exprDriven(CursorPosition position, ExpressionNode expression) {
    return new NodeWrapper<>(new ExpressionDrivenNode(expression, position));
  }

  protected static NodeWrapper<TranslateNode> translate(ExpressionNode key, CursorPosition position, @Nullable ExpressionNode fallback, NodeWrapper<?>... wrappedWiths) {
    List<MarkupNode> withs = new ArrayList<>();

    for (NodeWrapper<?> wrappedWith : wrappedWiths)
      withs.add(wrappedWith.get());

    return new NodeWrapper<>(new TranslateNode(key, toMarkupList(withs), fallback, position, new LinkedHashSet<>()));
  }

  private static MarkupList toMarkupList(List<MarkupNode> markupNodes) {
    return new MarkupList(Collections.emptyList()) {
      @Override
      public List<MarkupNode> get(Interpreter interpreter) {
        return markupNodes;
      }
    };
  }

  protected static NodeWrapper<InterpolationNode> interpolation(String expression, CursorPosition position) {
    return new NodeWrapper<>(new InterpolationNode(expr(expression), position));
  }

  protected static NodeWrapper<TextNode> text(String value, CursorPosition position) {
    return new NodeWrapper<>(new TextNode(value, position));
  }

  protected static void makeCase(TextWithAnchors input, NodeWrapper<?> wrappedExpectedNode) {
    MarkupNode actualNode = MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE);
    Assertions.assertEquals(Jsonifier.jsonify(wrappedExpectedNode.get()), Jsonifier.jsonify(actualNode));
  }
}
