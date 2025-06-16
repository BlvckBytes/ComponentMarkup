package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
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

  private static AExpression imm(String value) {
    return ImmediateExpression.of(value);
  }

  private static AExpression expr(String expression) {
    return expressionEvaluator.parseString(expression);
  }

  private static NodeWrapper container(TextWithAnchors text, int anchorIndex) {
    return new NodeWrapper(new ContainerNode(getAnchorPosition(text, anchorIndex), new ArrayList<>(), new ArrayList<>()));
  }

  private static NodeWrapper translate(AExpression key, TextWithAnchors text, int anchorIndex, @Nullable AstNode fallback, AstNode... with) {
    return new NodeWrapper(new TranslateNode(key, Arrays.asList(with), fallback, getAnchorPosition(text, anchorIndex), new ArrayList<>()));
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
