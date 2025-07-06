package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ast.BranchingNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import org.junit.jupiter.api.Test;

public class MarkupParserTests extends MarkupParserTestsBase {

  @Test
  public void shouldParseSimpleCase() {
    TextWithAnchors text = new TextWithAnchors(
      "@<translate",
      "  @let-a=\"b\"",
      "  [key]=\"my.expr\"",
      "  @[fallback]=\"'hello, ' & user\"",
      "/>"
    );

    makeCase(
      text,
      translate(
        expr("my.expr"),
        text.anchor(0),
        expr("'hello, ' & user")
      )
      .let("a", expr("b"), text.anchor(1))
    );
  }

  @Test
  public void shouldParseIf() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "<container *if=\"a\">@if contents</container>",
      "@after"
    );

    makeCase(
      text,
      container(CursorPosition.ZERO)
        .child(text("before", text.anchor(0)))
        .child(
          text("if contents", text.anchor(1))
            .ifCondition(expr("a"))
        )
        .child(text("after", text.anchor(2)))
    );
  }

  @Test
  public void shouldParseForLoop() {
    TextWithAnchors text = new TextWithAnchors(
      "@<container *for-member=\"members\">",
      "  @hello, @{{member}}@!",
      "</container>"
    );

    makeCase(
      text,
      forLoop(
        expr("members"),
        "member",
        container(text.anchor(0))
          .child(text("hello, ", text.anchor(1)))
          .child(interpolation("member", text.anchor(2)))
          .child(text("!", text.anchor(3))),
        null,
        null
      )
    );
  }

  @Test
  public void shouldParseForLoopWithConditional() {
    TextWithAnchors text = new TextWithAnchors(
      "@<container *for-member=\"members\" *if=\"member != null\">",
      "  @hello, @{{member}}@!",
      "</container>"
    );

    makeCase(
      text,
      forLoop(
        expr("members"),
        "member",
        container(text.anchor(0))
          .child(text("hello, ", text.anchor(1)))
          .child(interpolation("member", text.anchor(2)))
          .child(text("!", text.anchor(3)))
          .ifCondition(expr("member != null")),
        null,
        null
      )
    );
  }

  @Test
  public void shouldParseForLoopWithSeparatorAndReversed() {
    TextWithAnchors text = new TextWithAnchors(
      "<red",
      "  *for-member=\"members\"",
      "  for-separator={ <aqua>@separator }",
      "  for-reversed=true",
      ">@Hello, world"
    );

    makeCase(
      text,
      forLoop(
        expr("members"),
        "member",
        text("Hello, world", text.anchor(1))
          .color("red"),
        text("separator", text.anchor(0))
          .color("aqua"),
        imm(true)
      )
    );
  }

  @Test
  public void shouldParseIfElseIfElse() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "<container *if=\"a\">@if contents</container>",
      "<container *else-if=\"b\">@else-if b contents</container>",
      "<container *else-if=\"c\">@else-if c contents</container>",
      "<container *else>@else contents</container>",
      "@after"
    );

    makeCase(
      text,
      container(CursorPosition.ZERO)
        .child(text("before", text.anchor(0)))
        .child(
          ifElseIfElse(
            text("else contents", text.anchor(4)),
            text("if contents", text.anchor(1))
              .ifCondition(expr("a")),
            text("else-if b contents", text.anchor(2))
              .ifCondition(expr("b")),
            text("else-if c contents", text.anchor(3))
              .ifCondition(expr("c"))
          )
        )
        .child(text("after", text.anchor(5)))
    );
  }

  @Test
  public void shouldParseNestedIfElseIfElse() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "<container *if=\"a\">",
      "  <container *if=\"b\">@if a and b</container>",
      "  <container *else-if=\"d\">@if a and d</container>",
      "  <container *else>@if a and not b</container>",
      "</container>",
      "<container *else>",
      "  <container *if=\"c\">@if not a and c</container>",
      "  <container *else>@if not a and not c</container>",
      "</container>",
      "@after"
    );

    makeCase(
      text,
      container(CursorPosition.ZERO)
        .child(text("before", text.anchor(0)))
        .child(
          ifElseIfElse(
            ifElseIfElse(
              text("if not a and not c", text.anchor(5)),
              text("if not a and c", text.anchor(4))
                .ifCondition(expr("c"))
            ),
            ifElseIfElse(
              text("if a and not b", text.anchor(3)),
              text("if a and b", text.anchor(1))
                .ifCondition(expr("b")),
              text("if a and d", text.anchor(2))
                .ifCondition(expr("d"))
            )
              .ifCondition(expr("a"))
          )
        )
        .child(text("after", text.anchor(6)))
    );
  }

  @Test
  public void shouldParseWhenMatching() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "@<container *when=\"my.expression\">",
      "  <red *is=\"A\">@Case A</>",
      "  <green *is=\"B\">@Case B</>",
      "  @<blue *is=\"C\" *when=\"another.expression\">",
      "    <gold *is=\"D\">@Case D</>",
      "    <yellow *is=\"E\">@Case E</>",
      "  </>",
      "  <gray *other>@Fallback Case</>",
      "</>",
      "@after"
    );

    makeCase(
      text,
      container(CursorPosition.ZERO)
        .child(text("before", text.anchor(0)))
        .child(
          when(
            text.anchor(1),
            expr("my.expression"),
            text("Fallback Case", text.anchor(7))
              .color("gray"),
            whenMap(
              "A", text("Case A", text.anchor(2)).color("red"),
              "B", text("Case B", text.anchor(3)).color("green"),
              "C",
              container(text.anchor(4))
                .color("blue")
                .child(
                  when(
                    text.anchor(4),
                    expr("another.expression"),
                    null,
                    whenMap(
                      "D", text("Case D", text.anchor(5)).color("gold"),
                      "E", text("Case E", text.anchor(6)).color("yellow")
                    )
                  )
                )
            )
          )
        )
        .child(text("after", text.anchor(8)))
    );
  }

  @Test
  public void shouldCollapseStyleContainers() {
    TextWithAnchors text = new TextWithAnchors(
      "<red><bold><italic>@Hello, world!"
    );

    makeCase(
      text,
      text("Hello, world!", text.anchor(0))
        .color("red")
        .format(Format.BOLD, true)
        .format(Format.ITALIC, true)
    );
  }

  @Test
  public void shouldUnpackButInheritAll() {
    TextWithAnchors text = new TextWithAnchors(
      "<red *if=\"a\" *use=\"b\" @let-c=\"d\">@{{'test'}}"
    );

    makeCase(
      text,
      interpolation("'test'", text.anchor(1))
        .color(
          new BranchingNode(
            expr("b"),
            expr("'red'"),
            expr("null")
          )
        )
        .ifCondition(expr("a"))
        .let("c", expr("d"), text.anchor(0))
    );
  }

  @Test
  public void shouldAllowForLoopWithoutIterationVariable() {
    TextWithAnchors text = new TextWithAnchors(
      "<red *for=\"members\">@Hello, world!"
    );

    makeCase(
      text,
      forLoop(
        expr("members"),
        null,
        text("Hello, world!", text.anchor(0))
          .color("red"),
        null,
        null
      )
    );
  }

  @Test
  public void shouldHandleClosingTagShorthand() {
    TextWithAnchors text = new TextWithAnchors(
      "<aqua>@hello</aqua>",
      "<red><bold>@world</></>",
      "<green><italic>@test"
    );

    makeCase(
      text,
      container(CursorPosition.ZERO)
        .child(
          text("hello", text.anchor(0))
            .color("aqua")
        )
        .child(
          text("world", text.anchor(1))
            .format(Format.BOLD, true)
            .color("red")
        )
        .child(
          text("test", text.anchor(2))
            .format(Format.ITALIC, true)
            .color("green")
        )
    );
  }

  @Test
  public void shouldCloseVaryingCasingTags() {
    TextWithAnchors text = new TextWithAnchors(
      "<aQua>@hello</aQUA>"
    );

    makeCase(
      text,
        text("hello", text.anchor(0))
          .color("aqua")
    );
  }

  @Test
  public void shouldPreserveWhitespaceInBetweenTagsAndOrInterpolation() {
    TextWithAnchors text = new TextWithAnchors(
      "<red>@hello</red>@ @{{test}}"
    );

    makeCase(
      text,
      container(CursorPosition.ZERO)
        .child(
          text("hello", text.anchor(0))
            .color("red")
        )
        .child(
          text(" ", text.anchor(1))
        )
        .child(
          interpolation("test", text.anchor(2))
        )
    );

    text = new TextWithAnchors(
      "@<gray>@\\#@{{loop.index + 1}}@ <red>@{{word}}"
    );

    makeCase(
      text,
      container(text.anchor(0))
        .color("gray")
        .child(
          text("#", text.anchor(1))
        )
        .child(
          interpolation("loop.index + 1", text.anchor(2))
        )
        .child(
          text(" ", text.anchor(3))
        )
        .child(
          interpolation("word", text.anchor(4))
            .color("red")
        )
    );

    text = new TextWithAnchors(
      "@<gray>@\\#@{{loop.index + 1}} ",
      " <red>@{{word}}"
    );

    makeCase(
      text,
      container(text.anchor(0))
        .color("gray")
        .child(
          text("#", text.anchor(1))
        )
        .child(
          interpolation("loop.index + 1", text.anchor(2))
        )
        .child(
          interpolation("word", text.anchor(3))
            .color("red")
        )
    );
  }

  @Test
  public void shouldAllowToBindExpressionsToMarkupAttributes() {
    TextWithAnchors text = new TextWithAnchors(
      "@<translate key=\"my.key\" @[with]=\"a\"/>"
    );

    makeCase(
      text,
      translate(
        imm("my.key"),
        text.anchor(0),
        null,
        exprDriven(
          text.anchor(1),
          expr("a")
        )
      )
    );

    text = new TextWithAnchors(
      "@<translate key=\"my.key\" @[with]=\"a\" @[with]=\"b\"/>"
    );

    makeCase(
      text,
      translate(
        imm("my.key"),
        text.anchor(0),
        null,
        exprDriven(
          text.anchor(1),
          expr("a")
        ),
        exprDriven(
          text.anchor(2),
          expr("b")
        )
      )
    );
  }
}
