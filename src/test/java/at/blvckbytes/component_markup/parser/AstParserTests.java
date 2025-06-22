package at.blvckbytes.component_markup.parser;

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
}
