/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.BranchingNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.junit.jupiter.api.Test;

public class MarkupParserTests extends MarkupParserTestsBase {

  @Test
  public void shouldParseSimpleCase() {
    TextWithSubViews text = new TextWithSubViews(
      "<`translate´",
      "  *let-`a´=\"`b´\"",
      "  [key]=\"`my.expr´\"",
      "  [fallback]=\"`'hello, ' & user´\"",
      "/>"
    );

    makeCase(
      text,
      translate(
        expr(text.subView(3)),
        text.subView(0).setLowercase(),
        expr(text.subView(4))
      )
      .let(text.subView(1).setLowercase(), expr(text.subView(2)))
    );
  }

  @Test
  public void shouldParseIf() {
    TextWithSubViews text = new TextWithSubViews(
      "`before",
      "´<container *if=\"`a´\">`if contents´</container>",
      "`after´"
    );

    makeCase(
      text,
      container(text.initialView())
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
    TextWithSubViews text = new TextWithSubViews(
      "<`container´ `*for-`member´´=\"`members´\">",
      "  `hello, ´{`member´}`!",
      "´</container>"
    );

    makeCase(
      text,
      forLoop(
        text.subView(1),
        expr(text.subView(3)),
        text.subView(2),
        container(text.subView(0).setLowercase())
          .child(text(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(interpolation(text.subView(5)))
          .child(text(text.subView(6).setBuildFlags(SubstringFlag.INNER_TEXT))),
        null,
        null,
        null
      )
    );
  }

  @Test
  public void shouldParseForLoopWithConditional() {
    TextWithSubViews text = new TextWithSubViews(
      "<`container´ `*for-`member´´=\"`members´\" *if=\"`member neq null´\">",
      "  `hello, ´{`member´}`!",
      "´</container>"
    );

    makeCase(
      text,
      forLoop(
        text.subView(1),
        expr(text.subView(3)),
        text.subView(2),
        container(text.subView(0).setLowercase())
          .child(text(text.subView(5).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(interpolation(text.subView(6)))
          .child(text(text.subView(7).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .ifCondition(expr(text.subView(4))),
        null,
        null,
        null
      )
    );
  }

  @Test
  public void shouldParseForLoopWithSeparatorAndReversedAndEmpty() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´",
      "  `*for-`member´´=\"`members´\"",
      "  *for-separator={ <`aqua´>`separator ´}",
      "  *`for-reversed´",
      "  *for-empty={<`red´>`No entries!´}",
      ">`Hello, world´"
    );

    makeCase(
      text,
      forLoop(
        text.subView(1),
        expr(text.subView(3)),
        text.subView(2).setLowercase(),
        text(text.subView(9).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(0).setLowercase()),
        text(text.subView(5).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(4).setLowercase()),
        bool(text.subView(6).setLowercase(), true),
        text(text.subView(8).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(7).setLowercase())
      )
    );
  }

  @Test
  public void shouldParseIfElseIfElse() {
    TextWithSubViews text = new TextWithSubViews(
      "`before",
      "´<container *if=\"`a´\">`if contents´</container>",
      "<container *else-if=\"`b´\">`else-if b contents´</container>",
      "<container *else-if=\"`c´\">`else-if c contents´</container>",
      "<container *else>`else contents´</container>",
      "`after´"
    );

    makeCase(
      text,
      container(text.initialView())
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
    TextWithSubViews text = new TextWithSubViews(
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
      container(text.initialView())
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
    TextWithSubViews text = new TextWithSubViews(
      "`before",
      "´<`container´ *when=\"`my.expression´\">",
      "  <`red´ +is=\"`A´\">`Case A´</>",
      "  <`green´ +is=\"`B´\">`Case B´</>",
      "  <`blue´ +is=\"`C´\" *when=\"`another.expression´\">",
      "    <`gold´ +is=\"`D´\">`Case D´</>",
      "    <`yellow´ +is=\"`E´\">`Case E´</>",
      "  </>",
      "  <`gray´ *other>`Fallback Case´</>",
      "</>",
      "`after´"
    );

    makeCase(
      text,
      container(text.initialView())
        .child(text(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT)))
        .child(
          when(
            text.subView(1).setLowercase(),
            expr(text.subView(2)),
            text(text.subView(19).setBuildFlags(SubstringFlag.INNER_TEXT))
              .color(text.subView(18).setLowercase()),
            whenMap(
              text.subView(4),
              text(text.subView(5).setBuildFlags(SubstringFlag.INNER_TEXT))
                .color(text.subView(3).setLowercase()),
              text.subView(7),
              text(text.subView(8).setBuildFlags(SubstringFlag.INNER_TEXT))
                .color(text.subView(6).setLowercase()),
              text.subView(10),
              container(text.subView(9))
                .color(text.subView(9).setLowercase())
                .child(
                  when(
                    text.subView(9).setLowercase(),
                    expr(text.subView(11)),
                    null,
                    whenMap(
                      text.subView(13),
                      text(text.subView(14).setBuildFlags(SubstringFlag.INNER_TEXT))
                        .color(text.subView(12).setLowercase()),
                      text.subView(16),
                      text(text.subView(17).setBuildFlags(SubstringFlag.INNER_TEXT))
                        .color(text.subView(15).setLowercase())
                    )
                  )
                )
            )
          )
        )
        .child(text(text.subView(20).setBuildFlags(SubstringFlag.LAST_TEXT)))
    );
  }

  @Test
  public void shouldCollapseStyleContainers() {
    TextWithSubViews text = new TextWithSubViews(
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
    TextWithSubViews text = new TextWithSubViews(
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
    TextWithSubViews text = new TextWithSubViews(
      "<`red´ `*for´=\"`members´\">`Hello, world!´"
    );

    makeCase(
      text,
      forLoop(
        text.subView(1),
        expr(text.subView(2)),
        null,
        text(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT))
          .color(text.subView(0).setLowercase()),
        null,
        null,
        null
      )
    );
  }

  @Test
  public void shouldHandleClosingTagShorthand() {
    TextWithSubViews text = new TextWithSubViews(
      "<`aqua´>`hello´</aqua>",
      "<`red´><`bold´>`world´</></>",
      "<`green´><`italic´>`test´"
    );

    makeCase(
      text,
      container(text.initialView())
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
    TextWithSubViews text = new TextWithSubViews(
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
    TextWithSubViews text;

    String[] spaceCases = { " ", "  ", "   " };

    for (String spaceCase : spaceCases) {
      text = new TextWithSubViews("<`red´>`" + spaceCase + "´{`test´}");

      makeCase(
        text,
        container(text.subView(0))
          .color(text.subView(0).setLowercase())
          .child(text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(interpolation(text.subView(2)))
      );

      text = new TextWithSubViews("{`test´}`" + spaceCase + "´<`red´>`after´");

      makeCase(
        text,
        container(text.initialView())
          .child(interpolation(text.subView(0)))
          .child(text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(
            text(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT))
              .color(text.subView(2).setLowercase())
          )
      );

      text = new TextWithSubViews("<`red´>`" + spaceCase + "´<`aqua´>`after´");

      makeCase(
        text,
        container(text.subView(0))
          .child(text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(
            text(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT))
              .color(text.subView(2).setLowercase())
          )
          .color(text.subView(0).setLowercase())
      );

      text = new TextWithSubViews("{`test´}`" + spaceCase + "´{`test´}");

      makeCase(
        text,
        container(text.initialView())
          .child(interpolation(text.subView(0)))
          .child(text(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT)))
          .child(interpolation(text.subView(2)))
      );

      // Should not preserve whitespace if there was a linebreak
      text = new TextWithSubViews(
        "<`gray´>`#´{`loop.index + 1´}" + spaceCase,
        spaceCase + "<`red´>{`word´}"
      );

      makeCase(
        text,
        container(text.subView(0))
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
  }

  @Test
  public void shouldAllowToBindExpressionsToMarkupAttributes() {
    TextWithSubViews text = new TextWithSubViews(
      "<`translate´ key=\"`my.key´\" [with]=\"`a´\"/>"
    );

    makeCase(
      text,
      translate(
        string(text.subView(1)),
        text.subView(0).setLowercase(),
        null,
        exprDriven(text.subView(2))
      )
    );

    text = new TextWithSubViews(
      "<`translate´ key=\"`my.key´\" [with]=\"`a´\" [with]=\"`b´\"/>"
    );

    makeCase(
      text,
      translate(
        string(text.subView(1)),
        text.subView(0).setLowercase(),
        null,
        exprDriven(text.subView(2)),
        exprDriven(text.subView(3))
      )
    );
  }

  @Test
  public void shouldParseFlagAttributes() {
    TextWithSubViews text = new TextWithSubViews(
      "<`style´ `i´>`hello, world´"
    );

    makeCase(
      text,
      text(text.subView(2).setBuildFlags(SubstringFlag.LAST_TEXT))
        .format(Format.ITALIC, bool(text.subView(1).setLowercase(), true))
    );
  }
}
