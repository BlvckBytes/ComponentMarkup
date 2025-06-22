package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.node.control.ConditionalNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.control.IfThenElseNode;
import at.blvckbytes.component_markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.xml.XmlEventParser;
import at.blvckbytes.component_markup.xml.event.CursorPositionEvent;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
      container(anchor(text, -1))
        .child(
          translate(
            expr("my.expr"),
            anchor(text, 0),
            container(anchor(text, 2))
              .child(text(imm("hello, "), anchor(text, 3)))
              .child(text(expr("user"), anchor(text, 4)))
          )
          .let("a", expr("b"), anchor(text, 1))
        )
    );
  }

  @Test
  public void shouldParseIf() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "@<container *if=\"a\">@if contents</container>",
      "@after"
    );

    makeCase(
      text,
      container(anchor(text, -1))
        .child(text(imm("before"), anchor(text, 0)))
        .child(
          conditional(
            expr("a"),
            container(anchor(text, 1))
              .child(text(imm("if contents"), anchor(text, 2)))
          )
        )
        .child(text(imm("after"), anchor(text, 3)))
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
      container(anchor(text, -1))
        .child(text(imm("before"), anchor(text, 0)))
        .child(
          ifThenElse(
            container(anchor(text, 7))
              .child(text(imm("else contents"), anchor(text, 8))),
            conditional(
              expr("a"),
              container(anchor(text, 1))
                .child(text(imm("if contents"), anchor(text, 2)))
            ),
            conditional(
              expr("b"),
              container(anchor(text, 3))
                .child(text(imm("else-if b contents"), anchor(text, 4)))
            ),
            conditional(
              expr("c"),
              container(anchor(text, 5))
                .child(text(imm("else-if c contents"), anchor(text, 6)))
            )
          )
        )
        .child(text(imm("after"), anchor(text, 9)))
    );
  }

  @Test
  public void shouldParseNestedIfElse() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "@<container *if=\"a\">",
      "  @<container *if=\"b\">@if a and b</container>",
      "  @<container *else>@if a and not b</container>",
      "</container>",
      "@<container *else>",
      "  @<container *if=\"c\">@if not a and c</container>",
      "  @<container *else>@if not a and not c</container>",
      "</container>",
      "@after"
    );

    makeCase(
      text,
      container(anchor(text, -1))
        .child(text(imm("before"), anchor(text, 0)))
        .child(
          ifThenElse(
            container(anchor(text, 6))
              .child(
                ifThenElse(
                  container(anchor(text, 9))
                    .child(text(imm("if not a and not c"), anchor(text, 10))),
                  conditional(
                    expr("c"),
                    container(anchor(text, 7))
                      .child(text(imm("if not a and c"), anchor(text, 8)))
                  )
                )
              ),
            conditional(
              expr("a"),
              container(anchor(text, 1))
                .child(
                  ifThenElse(
                    container(anchor(text, 4))
                      .child(text(imm("if a and not b"), anchor(text, 5))),
                    conditional(
                      expr("b"),
                      container(anchor(text, 2))
                        .child(text(imm("if a and b"), anchor(text, 3)))
                    )
                  )
                )
            )
          )
        )
        .child(text(imm("after"), anchor(text, 11)))
    );
  }

  @SafeVarargs
  private static NodeWrapper<IfThenElseNode> ifThenElse(@Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<ConditionalNode>... wrappedConditions) {
    List<ConditionalNode> conditions = new ArrayList<>();

    for (NodeWrapper<ConditionalNode> wrappedCondition : wrappedConditions)
      conditions.add(wrappedCondition.get());

    return new NodeWrapper<>(new IfThenElseNode(conditions, wrappedFallback == null ? null : wrappedFallback.get()));
  }

  private static AExpression imm(String value) {
    return ImmediateExpression.of(value);
  }

  private static AExpression expr(String expression) {
    return expressionEvaluator.parseString(expression);
  }

  private static NodeWrapper<ConditionalNode> conditional(AExpression condition, NodeWrapper<?> wrappedBody) {
    return new NodeWrapper<>(new ConditionalNode(condition, wrappedBody.get(), new ArrayList<>()));
  }

  private static NodeWrapper<ContainerNode> container(CursorPosition position) {
    return new NodeWrapper<>(new ContainerNode(position, new ArrayList<>(), new ArrayList<>()));
  }

  private static NodeWrapper<TranslateNode> translate(AExpression key, CursorPosition position, @Nullable NodeWrapper<?> wrappedFallback, NodeWrapper<?>... wrappedWiths) {
    List<AstNode> withs = new ArrayList<>();

    for (NodeWrapper<?> wrappedWith : wrappedWiths)
      withs.add(wrappedWith.get());

    return new NodeWrapper<>(new TranslateNode(key, withs, wrappedFallback == null ? null : wrappedFallback.get(), position, new ArrayList<>()));
  }

  private static NodeWrapper<TextNode> text(AExpression value, CursorPosition position) {
    return new NodeWrapper<>(new TextNode(value, position, new ArrayList<>()));
  }

  private static CursorPosition anchor(TextWithAnchors text, int anchorIndex) {
    CursorPositionEvent positionEvent = text.getAnchor(anchorIndex);

    if (positionEvent == null)
      throw new IllegalStateException("Required anchor at index " + anchorIndex);

    return positionEvent.position;
  }

  private static void makeCase(TextWithAnchors input, NodeWrapper<?> wrappedExpectedAst) {
    AstParser parser = new AstParser(BuiltInTagRegistry.get(), expressionEvaluator);
    XmlEventParser.parse(input.text, parser);
    AstNode actualAst = parser.getResult();
    Assertions.assertEquals(wrappedExpectedAst.get().stringify(0), actualAst.stringify(0));
  }
}
