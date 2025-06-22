package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import org.junit.jupiter.api.Test;

public class AstParserTests extends AstParserTestsBase {

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
      container(CursorPosition.ZERO)
        .child(
          translate(
            expr("my.expr"),
            text.anchor(0),
            container(text.anchor(2))
              .child(text(imm("hello, "), text.anchor(3)))
              .child(text(expr("user"), text.anchor(4)))
          )
          .let("a", expr("b"), text.anchor(1))
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
      container(CursorPosition.ZERO)
        .child(text(imm("before"), text.anchor(0)))
        .child(
          conditional(
            expr("a"),
            container(text.anchor(1))
              .child(text(imm("if contents"), text.anchor(2)))
          )
        )
        .child(text(imm("after"), text.anchor(3)))
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
      container(CursorPosition.ZERO)
        .child(
          forLoop(
            expr("members"),
            "member",
            container(text.anchor(0))
              .child(text(imm("hello, "), text.anchor(1)))
              .child(text(expr("member"), text.anchor(2)))
              .child(text(imm("!"), text.anchor(3)))
          )
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
      container(CursorPosition.ZERO)
        .child(
          forLoop(
            expr("members"),
            "member",
            conditional(
              expr("member != null"),
              container(text.anchor(0))
                .child(text(imm("hello, "), text.anchor(1)))
                .child(text(expr("member"), text.anchor(2)))
                .child(text(imm("!"), text.anchor(3)))
            )
          )
        )
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
      container(CursorPosition.ZERO)
        .child(text(imm("before"), text.anchor(0)))
        .child(
          ifThenElse(
            container(text.anchor(7))
              .child(text(imm("else contents"), text.anchor(8))),
            conditional(
              expr("a"),
              container(text.anchor(1))
                .child(text(imm("if contents"), text.anchor(2)))
            ),
            conditional(
              expr("b"),
              container(text.anchor(3))
                .child(text(imm("else-if b contents"), text.anchor(4)))
            ),
            conditional(
              expr("c"),
              container(text.anchor(5))
                .child(text(imm("else-if c contents"), text.anchor(6)))
            )
          )
        )
        .child(text(imm("after"), text.anchor(9)))
    );
  }

  @Test
  public void shouldParseNestedIfThenElse() {
    TextWithAnchors text = new TextWithAnchors(
      "@before",
      "@<container *if=\"a\">",
      "  @<container *if=\"b\">@if a and b</container>",
      "  @<container *else-if=\"d\">@if a and d</container>",
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
      container(CursorPosition.ZERO)
        .child(text(imm("before"), text.anchor(0)))
        .child(
          ifThenElse(
            container(text.anchor(8))
              .child(
                ifThenElse(
                  container(text.anchor(11))
                    .child(text(imm("if not a and not c"), text.anchor(12))),
                  conditional(
                    expr("c"),
                    container(text.anchor(9))
                      .child(text(imm("if not a and c"), text.anchor(10)))
                  )
                )
              ),
            conditional(
              expr("a"),
              container(text.anchor(1))
                .child(
                  ifThenElse(
                    container(text.anchor(6))
                      .child(text(imm("if a and not b"), text.anchor(7))),
                    conditional(
                      expr("b"),
                      container(text.anchor(2))
                        .child(text(imm("if a and b"), text.anchor(3)))
                    ),
                    conditional(
                      expr("d"),
                      container(text.anchor(4))
                        .child(text(imm("if a and d"), text.anchor(5)))
                    )
                  )
                )
            )
          )
        )
        .child(text(imm("after"), text.anchor(13)))
    );
  }
}
