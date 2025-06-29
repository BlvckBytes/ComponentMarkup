package at.blvckbytes.component_markup.markup.parser;

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
        .child(text(imm("before"), text.anchor(0)))
        .child(
          conditional(
            expr("a"),
            text(imm("if contents"), text.anchor(1))
          )
        )
        .child(text(imm("after"), text.anchor(2)))
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
          .child(text(imm("hello, "), text.anchor(1)))
          .child(text(expr("member"), text.anchor(2)))
          .child(text(imm("!"), text.anchor(3))),
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
        conditional(
          expr("member != null"),
          container(text.anchor(0))
            .child(text(imm("hello, "), text.anchor(1)))
            .child(text(expr("member"), text.anchor(2)))
            .child(text(imm("!"), text.anchor(3)))
        ),
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
        text(imm("Hello, world"), text.anchor(1))
          .color("red"),
        text(imm("separator"), text.anchor(0))
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
        .child(text(imm("before"), text.anchor(0)))
        .child(
          ifElseIfElse(
            text(imm("else contents"), text.anchor(4)),
            conditional(
              expr("a"),
              text(imm("if contents"), text.anchor(1))
            ),
            conditional(
              expr("b"),
              text(imm("else-if b contents"), text.anchor(2))
            ),
            conditional(
              expr("c"),
              text(imm("else-if c contents"), text.anchor(3))
            )
          )
        )
        .child(text(imm("after"), text.anchor(5)))
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
        .child(text(imm("before"), text.anchor(0)))
        .child(
          ifElseIfElse(
            ifElseIfElse(
              text(imm("if not a and not c"), text.anchor(5)),
              conditional(
                expr("c"),
                text(imm("if not a and c"), text.anchor(4))
              )
            ),
            conditional(
              expr("a"),
              ifElseIfElse(
                text(imm("if a and not b"), text.anchor(3)),
                conditional(
                  expr("b"),
                  text(imm("if a and b"), text.anchor(1))
                ),
                conditional(
                  expr("d"),
                  text(imm("if a and d"), text.anchor(2))
                )
              )
            )
          )
        )
        .child(text(imm("after"), text.anchor(6)))
    );
  }

  @Test
  public void shouldCollapseStyleContainers() {
    TextWithAnchors text = new TextWithAnchors(
      "<red><bold><italic>@Hello, world!"
    );

    makeCase(
      text,
      text(imm("Hello, world!"), text.anchor(0))
        .color("red")
        .format(Format.BOLD, true)
        .format(Format.ITALIC, true)
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
        text(imm("Hello, world!"), text.anchor(0))
          .color("red"),
        null,
        null
      )
    );
  }

  @Test
  public void shouldAllowToCloseAllCurrentlyOpenTags() {
    TextWithAnchors text = new TextWithAnchors(
      "<aqua>@hello</aqua>",
      "<red><bold>@world</>",
      "<green><italic>@test"
    );

    makeCase(
      text,
      container(CursorPosition.ZERO)
        .child(
          text(imm("hello"), text.anchor(0))
            .color("aqua")
        )
        .child(
          text(imm("world"), text.anchor(1))
            .format(Format.BOLD, true)
            .color("red")
        )
        .child(
          text(imm("test"), text.anchor(2))
            .format(Format.ITALIC, true)
            .color("green")
        )
    );
  }
}
