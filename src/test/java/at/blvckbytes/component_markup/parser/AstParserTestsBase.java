package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.node.control.ConditionalNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public abstract class AstParserTestsBase {

  protected static NodeWrapper<ForLoopNode> forLoop(ExpressionNode iterable, String iterationVariable, NodeWrapper<?> wrappedBody, @Nullable NodeWrapper<?> wrappedSeparator, @Nullable ExpressionNode reversed) {
    return new NodeWrapper<>(new ForLoopNode(iterable, iterationVariable, wrappedBody.get(), wrappedSeparator == null ? null : wrappedSeparator.get(), reversed, new ArrayList<>()));
  }

  @SafeVarargs
  protected static NodeWrapper<IfElseIfElseNode> ifElseIfElse(@Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<ConditionalNode>... wrappedConditions) {
    List<ConditionalNode> conditions = new ArrayList<>();

    for (NodeWrapper<ConditionalNode> wrappedCondition : wrappedConditions)
      conditions.add(wrappedCondition.get());

    return new NodeWrapper<>(new IfElseIfElseNode(conditions, wrappedFallback == null ? null : wrappedFallback.get()));
  }

  protected static ExpressionNode imm(boolean value) {
    return ImmediateExpression.of(value);
  }

  protected static ExpressionNode imm(String value) {
    return ImmediateExpression.of(value);
  }

  protected static ExpressionNode expr(String expression) {
    return ExpressionParser.parse(expression);
  }

  protected static NodeWrapper<ConditionalNode> conditional(ExpressionNode condition, NodeWrapper<?> wrappedBody) {
    return new NodeWrapper<>(new ConditionalNode(condition, wrappedBody.get(), new ArrayList<>()));
  }

  protected static NodeWrapper<ContainerNode> container(CursorPosition position) {
    return new NodeWrapper<>(new ContainerNode(position, new ArrayList<>(), new ArrayList<>()));
  }

  protected static NodeWrapper<TranslateNode> translate(ExpressionNode key, CursorPosition position, @Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<?>... wrappedWiths) {
    List<MarkupNode> withs = new ArrayList<>();

    for (NodeWrapper<?> wrappedWith : wrappedWiths)
      withs.add(wrappedWith.get());

    return new NodeWrapper<>(new TranslateNode(key, withs, wrappedFallback == null ? null : wrappedFallback.get(), position, new ArrayList<>()));
  }

  protected static NodeWrapper<TextNode> text(ExpressionNode value, CursorPosition position) {
    return new NodeWrapper<>(new TextNode(value, position, new ArrayList<>()));
  }

  protected static void makeCase(TextWithAnchors input, NodeWrapper<?> wrappedExpectedAst) {
    MarkupNode actualAst = AstParser.parse(input.text, BuiltInTagRegistry.get());
    Assertions.assertEquals(wrappedExpectedAst.get().toString(), actualAst.toString());
  }
}
