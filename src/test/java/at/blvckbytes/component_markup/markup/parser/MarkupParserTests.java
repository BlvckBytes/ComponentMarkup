package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.BranchingNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.junit.jupiter.api.Test;

public class MarkupParserTests extends MarkupParserTestsBase {

  @Test
  public void shouldParseSimpleCase() {
    TextWithAnchors text = new TextWithAnchors(
      "<@translate",
      "  @*let-`a´=\"`b´\"",
      "  [key]=\"`my.expr´\"",
      "  @[fallback]=\"`'hello, ' & user´\"",
      "/>"
    );

    makeCase(
      text,
      translate(
        expr(text.subView(2)),
        text.anchor(0),
        expr(text.subView(3))
      )
      .let(text.subView(0).setLowercase(), expr(text.subView(1)))
    );
  }

  @Test
  public void shouldParseIf() {
    TextWithAnchors text = new TextWithAnchors(
      "`before",
      "´<container *if=\"`a´\">`if contents´</container>",
      "`after´"
    );

    makeCase(
      text,
      container(0)
        .child(text(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT)))
        .child(
          text(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT))
            .ifCondition(expr(text.subView(1)))
        )
        .child(text(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT)))
    );
  }

  @Test
  public void shouldParseForLoop() {
    TextWithAnchors text = new TextWithAnchors(
      "<@container *for-`member´=\"`members´\">",
      "  `hello, ´{`member´}`!",
      "´</container>"
    );

    makeCase(
      text,
      forLoop(
        expr(text.subView(1)),
        text.subView(0),
        container(text.anchor(0))
          .child(text(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(interpolation(text.subView(3)))
          .child(text(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT))),
        null,
        null,
        null
      )
    );
  }

  @Test
  public void shouldParseForLoopWithConditional() {
    TextWithAnchors text = new TextWithAnchors(
      "<@container *for-`member´=\"`members´\" *if=\"`member != null´\">",
      "  `hello, ´{`member´}`!",
      "´</container>"
    );

    makeCase(
      text,
      forLoop(
        expr(text.subView(1)),
        text.subView(0),
        container(text.anchor(0))
          .child(text(text.subView(3).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(interpolation(text.subView(4)))
          .child(text(text.subView(5).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .ifCondition(expr(text.subView(2))),
        null,
        null,
        null
      )
    );
  }

  @Test
  public void shouldParseForLoopWithSeparatorAndReversedAndEmpty() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´",
      "  *for-`member´=\"`members´\"",
      "  *for-separator={ <`aqua´>`separator ´}",
      "  *for-reversed=`true´",
      "  *for-empty={<`red´>`No entries!´}",
      ">`Hello, world´"
    );

    makeCase(
      text,
      forLoop(
        expr(text.subView(2)),
        text.subView(1).setLowercase(),
        text(text.subView(8).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(0).setLowercase()),
        text(text.subView(4).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(3).setLowercase()),
        bool(text.subView(5), true),
        text(text.subView(7).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(6).setLowercase())
      )
    );
  }

  @Test
  public void shouldParseIfElseIfElse() {
    TextWithAnchors text = new TextWithAnchors(
      "`before",
      "´<container *if=\"`a´\">`if contents´</container>",
      "<container *else-if=\"`b´\">`else-if b contents´</container>",
      "<container *else-if=\"`c´\">`else-if c contents´</container>",
      "<container *else>`else contents´</container>",
      "`after´"
    );

    makeCase(
      text,
      container(0)
        .child(text(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT)))
        .child(
          ifElseIfElse(
            text(text.subView(7).setBuildFlags(SubstringFlag.INNER_TEXT)),
            text(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT))
              .ifCondition(expr(text.subView(1))),
            text(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT))
              .ifCondition(expr(text.subView(3))),
            text(text.subView(6).setBuildFlags(SubstringFlag.INNER_TEXT))
              .ifCondition(expr(text.subView(5)))
          )
        )
        .child(text(text.subView(8).setBuildFlags(SubstringFlag.LAST_TEXT)))
    );
  }

  @Test
  public void shouldParseNestedIfElseIfElse() {
    TextWithAnchors text = new TextWithAnchors(
      "`before",
      "´<container *if=\"`a´\">",
      "  <container *if=\"`b´\">`if a and b´</container>",
      "  <container *else-if=\"`d´\">`if a and d´</container>",
      "  <container *else>`if a and not b´</container>",
      "</container>",
      "<container *else>",
      "  <container *if=\"`c´\">`if not a and c´</container>",
      "  <container *else>`if not a and not c´</container>",
      "</container>",
      "`after´"
    );

    makeCase(
      text,
      container(0)
        .child(text(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT)))
        .child(
          ifElseIfElse(
            ifElseIfElse(
              text(text.subView(9).setBuildFlags(SubstringFlag.INNER_TEXT)),
              text(text.subView(8).setBuildFlags(SubstringFlag.INNER_TEXT))
                .ifCondition(expr(text.subView(7)))
            ),
            ifElseIfElse(
              text(text.subView(6).setBuildFlags(SubstringFlag.INNER_TEXT)),
              text(text.subView(3).setBuildFlags(SubstringFlag.INNER_TEXT))
                .ifCondition(expr(text.subView(2))),
              text(text.subView(5).setBuildFlags(SubstringFlag.INNER_TEXT))
                .ifCondition(expr(text.subView(4)))
            )
              .ifCondition(expr(text.subView(1)))
          )
        )
        .child(text(text.subView(10).setBuildFlags(SubstringFlag.LAST_TEXT)))
    );
  }

  @Test
  public void shouldParseWhenMatching() {
    TextWithAnchors text = new TextWithAnchors(
      "`before",
      "´<@container *when=\"`my.expression´\">",
      "  <`red´ +is=\"`A´\">`Case A´</>",
      "  <`green´ +is=\"`B´\">`Case B´</>",
      "  <@`blue´ +is=\"`C´\" *when=\"`another.expression´\">",
      "    <`gold´ +is=\"`D´\">`Case D´</>",
      "    <`yellow´ +is=\"`E´\">`Case E´</>",
      "  </>",
      "  <`gray´ *other>`Fallback Case´</>",
      "</>",
      "`after´"
    );

    makeCase(
      text,
      container(0)
        .child(text(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT)))
        .child(
          when(
            text.anchor(0),
            expr(text.subView(1)),
            text(text.subView(18).setBuildFlags(SubstringFlag.INNER_TEXT))
              .color(text.subView(17).setLowercase()),
            whenMap(
              text.subView(3),
              text(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT))
                .color(text.subView(2).setLowercase()),
              text.subView(6),
              text(text.subView(7).setBuildFlags(SubstringFlag.INNER_TEXT))
                .color(text.subView(5).setLowercase()),
              text.subView(9),
              container(text.anchor(1))
                .color(text.subView(8).setLowercase())
                .child(
                  when(
                    text.anchor(1),
                    expr(text.subView(10)),
                    null,
                    whenMap(
                      text.subView(12),
                      text(text.subView(13).setBuildFlags(SubstringFlag.INNER_TEXT))
                        .color(text.subView(11).setLowercase()),
                      text.subView(15),
                      text(text.subView(16).setBuildFlags(SubstringFlag.INNER_TEXT))
                        .color(text.subView(14).setLowercase())
                    )
                  )
                )
            )
          )
        )
        .child(text(text.subView(19).setBuildFlags(SubstringFlag.LAST_TEXT)))
    );
  }

  @Test
  public void shouldCollapseStyleContainers() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´><`bold´><`italic´>`Hello, world!´"
    );

    makeCase(
      text,
      text(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT))
        .color(text.subView(0).setLowercase())
        .format(Format.BOLD, bool(text.subView(1).setLowercase(), true))
        .format(Format.ITALIC, bool(text.subView(2).setLowercase(), true))
    );
  }

  @Test
  public void shouldUnpackButInheritAll() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´ *if=\"`a´\" *use=\"`b´\" *let-`c´=\"`d´\">{`'test'´}"
    );

    makeCase(
      text,
      interpolation(text.subView(5))
        .color(
          new BranchingNode(
            expr(text.subView(2)),
            null,
            string(text.subView(0).setLowercase()),
            null,
            ImmediateExpression.ofNull()
          )
        )
        .ifCondition(expr(text.subView(1)))
        .let(text.subView(3).setLowercase(), expr(text.subView(4)))
    );
  }

  @Test
  public void shouldAllowForLoopWithoutIterationVariable() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´ *for=\"`members´\">`Hello, world!´"
    );

    makeCase(
      text,
      forLoop(
        expr(text.subView(1)),
        null,
        text(text.subView(2).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(0).setLowercase()),
        null,
        null,
        null
      )
    );
  }

  @Test
  public void shouldHandleClosingTagShorthand() {
    TextWithAnchors text = new TextWithAnchors(
      "<`aqua´>`hello´</aqua>",
      "<`red´><`bold´>`world´</></>",
      "<`green´><`italic´>`test´"
    );

    makeCase(
      text,
      container(0)
        .child(
          text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT))
            .color(text.subView(0).setLowercase())
        )
        .child(
          text(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT))
            .format(Format.BOLD, bool(text.subView(3).setLowercase(), true))
            .color(text.subView(2).setLowercase())
        )
        .child(
          text(text.subView(7).setBuildFlags(SubstringFlag.LAST_TEXT))
            .format(Format.ITALIC, bool(text.subView(6).setLowercase(), true))
            .color(text.subView(5).setLowercase())
        )
    );
  }

  @Test
  public void shouldCloseVaryingCasingTags() {
    TextWithAnchors text = new TextWithAnchors(
      "<`aQua´>`hello´</aQUA>"
    );

    makeCase(
      text,
        text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT))
          .color(text.subView(0).setLowercase())
    );
  }

  @Test
  public void shouldPreserveWhitespaceInBetweenTagsAndOrInterpolation() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´>`hello´</red>` ´{`test´}"
    );

    makeCase(
      text,
      container(0)
        .child(
          text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT))
            .color(text.subView(0).setLowercase())
        )
        .child(
          text(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT))
        )
        .child(
          interpolation(text.subView(3))
        )
    );

    text = new TextWithAnchors(
      "<@`gray´>`#´{`loop.index + 1´}` ´<`red´>{`word´}"
    );

    makeCase(
      text,
      container(text.anchor(0))
        .color(text.subView(0).setLowercase())
        .child(
          text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT))
        )
        .child(
          interpolation(text.subView(2))
        )
        .child(
          text(text.subView(3).setBuildFlags(SubstringFlag.INNER_TEXT))
        )
        .child(
          interpolation(text.subView(5))
            .color(text.subView(4).setLowercase())
        )
    );

    text = new TextWithAnchors(
      "<@`gray´>`#´{`loop.index + 1´} ",
      " <`red´>{`word´}"
    );

    makeCase(
      text,
      container(text.anchor(0))
        .color(text.subView(0).setLowercase())
        .child(
          text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT))
        )
        .child(
          interpolation(text.subView(2))
        )
        .child(
          interpolation(text.subView(4))
            .color(text.subView(3).setLowercase())
        )
    );
  }

  @Test
  public void shouldAllowToBindExpressionsToMarkupAttributes() {
    TextWithAnchors text = new TextWithAnchors(
      "<@translate key=\"`my.key´\" [with]=\"`a´\"/>"
    );

    makeCase(
      text,
      translate(
        string(text.subView(0)),
        text.anchor(0),
        null,
        exprDriven(expr(text.subView(1))
        )
      )
    );

    text = new TextWithAnchors(
      "<@translate key=\"`my.key´\" [with]=\"`a´\" [with]=\"`b´\"/>"
    );

    makeCase(
      text,
      translate(
        string(text.subView(0)),
        text.anchor(0),
        null,
        exprDriven(expr(text.subView(1))),
        exprDriven(expr(text.subView(2)))
      )
    );
  }
}
