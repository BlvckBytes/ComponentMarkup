package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TranslateNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public abstract class MarkupParserTestsBase {

  protected static NodeWrapper<ForLoopNode> forLoop(ExpressionNode iterable, @Nullable String iterationVariable, NodeWrapper<?> wrappedBody, @Nullable NodeWrapper<?> wrappedSeparator, @Nullable ExpressionNode reversed) {
    return new NodeWrapper<>(new ForLoopNode(iterable, iterationVariable, wrappedBody.get(), wrappedSeparator == null ? null : wrappedSeparator.get(), reversed, new ArrayList<>()));
  }

  @SafeVarargs
  protected static NodeWrapper<IfElseIfElseNode> ifElseIfElse(@Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<? extends MarkupNode>... wrappedConditions) {
    List<MarkupNode> conditions = new ArrayList<>();

    for (NodeWrapper<? extends MarkupNode> wrappedCondition : wrappedConditions)
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

  protected static NodeWrapper<ContainerNode> container(CursorPosition position) {
    return new NodeWrapper<>(new ContainerNode(position, new ArrayList<>(), new ArrayList<>()));
  }

  protected static NodeWrapper<TranslateNode> translate(ExpressionNode key, CursorPosition position, @Nullable ExpressionNode fallback, NodeWrapper<?>... wrappedWiths) {
    List<MarkupNode> withs = new ArrayList<>();

    for (NodeWrapper<?> wrappedWith : wrappedWiths)
      withs.add(wrappedWith.get());

    return new NodeWrapper<>(new TranslateNode(key, withs, fallback, position, new ArrayList<>()));
  }

  protected static NodeWrapper<TextNode> text(ExpressionNode value, CursorPosition position) {
    return new NodeWrapper<>(new TextNode(value, position, new ArrayList<>()));
  }

  protected static void makeCase(TextWithAnchors input, NodeWrapper<?> wrappedExpectedNode) {
    MarkupNode actualNode = MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE);
    Assertions.assertEquals(wrappedExpectedNode.get().toString(), actualNode.toString());
  }
}
