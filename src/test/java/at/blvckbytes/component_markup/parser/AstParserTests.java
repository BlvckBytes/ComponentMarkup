package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.node.control.ConditionalNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.control.IfThenElseNode;
import at.blvckbytes.component_markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.xml.XmlEventParser;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import static at.blvckbytes.component_markup.parser.NodeWrapper.getAnchorPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AstParserTests {

  private static final IExpressionEvaluator expressionEvaluator = new GPEEE(Logger.getAnonymousLogger());

  @Test
  public void shouldParseSimpleCase() {
    TextWithAnchors text = new TextWithAnchors(
      "@<translate",
      "  @let-a=\"b\"",
      "  [key]=\"my.expr\"",
      "  @fallback={",
      "    @hello, @{{user}}",
      "  }",
      "/>"
    );

    makeCase(
      text,
      container(text, -1)
        .child(
          translate(
            expr("my.expr"),
            text, 0,
            container(text, 2)
              .child(text(imm("hello, "), text, 3))
              .child(text(expr("user"), text, 4))
              .get()
          )
          .let("a", expr("b"), text, 1)
          .get()
        )
        .get()
    );
  }

  @Test
  public void shouldParseIfThenElse() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "@<container *if=\"a\">@if contents</container>",
      "@<container *else-if=\"b\">@else-if b contents</container>",
      "@<container *else-if=\"c\">@else-if c contents</container>",
      "@<container *else>@else contents</container>",
      "@after"
    );

    makeCase(
      text,
      container(text, -1)
        .child(text(imm("before"), text, 0))
        .child(new IfThenElseNode(
          Arrays.asList(
            conditional(
              expr("a"),
              container(text, 1)
                .child(text(imm("if contents"), text, 2))
                .get()
            )
              .get(),
            conditional(
              expr("b"),
              container(text, 3)
                .child(text(imm("else-if b contents"), text, 4))
                .get()
            )
              .get(),
            conditional(
              expr("c"),
              container(text, 5)
                .child(text(imm("else-if c contents"), text, 6))
                .get()
            )
              .get()
          ),
          container(text, 7)
            .child(text(imm("else contents"), text, 8))
            .get()
        ))
        .child(text(imm("after"), text, 9))
        .get()
    );
  }

  private static AExpression imm(String value) {
    return ImmediateExpression.of(value);
  }

  private static AExpression expr(String expression) {
    return expressionEvaluator.parseString(expression);
  }

  private static NodeWrapper<ConditionalNode> conditional(AExpression condition, AstNode body) {
    return new NodeWrapper<>(new ConditionalNode(condition, body, new ArrayList<>()));
  }

  private static NodeWrapper<ContainerNode> container(TextWithAnchors text, int anchorIndex) {
    return new NodeWrapper<>(new ContainerNode(getAnchorPosition(text, anchorIndex), new ArrayList<>(), new ArrayList<>()));
  }

  private static NodeWrapper<TranslateNode> translate(AExpression key, TextWithAnchors text, int anchorIndex, @Nullable AstNode fallback, AstNode... with) {
    return new NodeWrapper<>(new TranslateNode(key, Arrays.asList(with), fallback, getAnchorPosition(text, anchorIndex), new ArrayList<>()));
  }

  private static TextNode text(AExpression value, TextWithAnchors text, int anchorIndex) {
    return new TextNode(value, getAnchorPosition(text, anchorIndex), new ArrayList<>());
  }

  private static void makeCase(TextWithAnchors input, AstNode expectedAst) {
    AstParser parser = new AstParser(BuiltInTagRegistry.get(), expressionEvaluator);
    XmlEventParser.parse(input.text, parser);
    AstNode actualAst = parser.getResult();
    assertEquals(expectedAst.stringify(0), actualAst.stringify(0));
  }
}
