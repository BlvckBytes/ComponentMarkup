package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.node.control.ConditionalNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.control.ForLoopNode;
import at.blvckbytes.component_markup.ast.node.control.IfElseIfElseNode;
import at.blvckbytes.component_markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.xml.XmlEventParser;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class AstParserTestsBase {

  private static final IExpressionEvaluator expressionEvaluator = new GPEEE(Logger.getAnonymousLogger());

  protected static NodeWrapper<ForLoopNode> forLoop(AExpression iterable, String iterationVariable, NodeWrapper<?> wrappedBody, @Nullable NodeWrapper<?> wrappedSeparator, @Nullable AExpression reversed) {
    return new NodeWrapper<>(new ForLoopNode(iterable, iterationVariable, wrappedBody.get(), wrappedSeparator == null ? null : wrappedSeparator.get(), reversed, new ArrayList<>()));
  }

  @SafeVarargs
  protected static NodeWrapper<IfElseIfElseNode> ifElseIfElse(@Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<ConditionalNode>... wrappedConditions) {
    List<ConditionalNode> conditions = new ArrayList<>();

    for (NodeWrapper<ConditionalNode> wrappedCondition : wrappedConditions)
      conditions.add(wrappedCondition.get());

    return new NodeWrapper<>(new IfElseIfElseNode(conditions, wrappedFallback == null ? null : wrappedFallback.get()));
  }

  protected static AExpression imm(boolean value) {
    return ImmediateExpression.of(value);
  }

  protected static AExpression imm(String value) {
    return ImmediateExpression.of(value);
  }

  protected static AExpression expr(String expression) {
    return expressionEvaluator.parseString(expression);
  }

  protected static NodeWrapper<ConditionalNode> conditional(AExpression condition, NodeWrapper<?> wrappedBody) {
    return new NodeWrapper<>(new ConditionalNode(condition, wrappedBody.get(), new ArrayList<>()));
  }

  protected static NodeWrapper<ContainerNode> container(CursorPosition position) {
    return new NodeWrapper<>(new ContainerNode(position, new ArrayList<>(), new ArrayList<>()));
  }

  protected static NodeWrapper<TranslateNode> translate(AExpression key, CursorPosition position, @Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<?>... wrappedWiths) {
    List<AstNode> withs = new ArrayList<>();

    for (NodeWrapper<?> wrappedWith : wrappedWiths)
      withs.add(wrappedWith.get());

    return new NodeWrapper<>(new TranslateNode(key, withs, wrappedFallback == null ? null : wrappedFallback.get(), position, new ArrayList<>()));
  }

  protected static NodeWrapper<TextNode> text(AExpression value, CursorPosition position) {
    return new NodeWrapper<>(new TextNode(value, position, new ArrayList<>()));
  }

  protected static void makeCase(TextWithAnchors input, NodeWrapper<?> wrappedExpectedAst) {
    AstParser parser = new AstParser(BuiltInTagRegistry.get(), expressionEvaluator);
    XmlEventParser.parse(input.text, parser);
    AstNode actualAst = parser.getResult();
    Assertions.assertEquals(wrappedExpectedAst.get().toString(), actualAst.toString());
  }
}
