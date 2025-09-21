/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.test_utils.ListBuilder;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TokenOutputTests {

  @Test
  public void shouldTokenizeComments() {
    makeCommentCase("<!-- Hello, world! :) -->");
    makeCommentCase("<!-- Hello, - world! :) -->");
    makeCommentCase("<!-- Hello, -- world! :) -->");
    makeCommentCase("<!-- Hello, -- > world! :) -->");

    makeCommentCase(
      "<!-- This is",
      "a multiline",
      "comment :) --",
      "> :') -->"
    );
  }

  @Test
  public void shouldTokenizeNestedComments() {
    TextWithSubViews text = new TextWithSubViews(
      "``<!-- Hello, world!",
      "this is a ´`<!-- nested comment",
      "case! -->´` which is",
      "very convenient -->´´"
    );

    makeSequenceCase(
      InputView.of(text.text),
      new ListBuilder<>(HierarchicalToken.class)
        .add(
          new HierarchicalToken(TokenType.MARKUP__COMMENT, text.subView(0))
            .addChild(new HierarchicalToken(TokenType.MARKUP__COMMENT, text.subView(2)))
        ),
      new ListBuilder<>(Token.class)
        .add(new Token(TokenType.MARKUP__COMMENT, text.subView(1)))
        .add(new Token(TokenType.MARKUP__COMMENT, text.subView(2)))
        .add(new Token(TokenType.MARKUP__COMMENT, text.subView(3)))
    );
  }

  @Test
  public void shouldTokenizeNestedInterpolations() {
    TextWithSubViews text = new TextWithSubViews(
      "`pre` ´´`{`a´` ´`+´` ´`×`hello` ´`{`×``{`a´}´` ´`{`b´}´×`´}´` ´world×`´}´`` ´post´"
    );

    makeHierarchicalCase(
      InputView.of(text.text),
      new ListBuilder<>(HierarchicalToken.class)
        .add(
          new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(1)))
        )
        .add(
          new HierarchicalToken(TokenType.ANY__INTERPOLATION, text.subView(2))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(3)))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(4)))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, text.subView(5)))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(6)))
            .addChild(
              new HierarchicalToken(TokenType.EXPRESSION__STRING, text.subView(7))
                .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(8)))
                .addChild(
                  new HierarchicalToken(TokenType.ANY__INTERPOLATION, text.subView(9))
                    .addChild(
                      new HierarchicalToken(TokenType.EXPRESSION__STRING, text.subView(10))
                        .addChild(
                          new HierarchicalToken(TokenType.ANY__INTERPOLATION, text.subView(11))
                            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(12)))
                        )
                        .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(13)))
                        .addChild(
                          new HierarchicalToken(TokenType.ANY__INTERPOLATION, text.subView(14))
                            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(15)))
                        )
                    )
                )
                .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(16)))
            )
        )
        .add(
          new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.subView(17).setBuildFlags(SubstringFlag.LAST_TEXT))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(18)))
        )
    );
  }

  private void makeCommentCase(String... lines) {
    String[] finalLines = new String[lines.length];

    for (int i = 0; i < finalLines.length; ++i) {
      String lineValue = lines[i];

      if (i == 0)
        lineValue = "`<´`red´`>´`" + lineValue;

      if (i == finalLines.length - 1)
        lineValue += "´`<´`aqua´`>´";

      finalLines[i] = lineValue;
    }

    TextWithSubViews text = new TextWithSubViews(finalLines);

    makeHierarchicalCase(
      InputView.of(text.text),
      new ListBuilder<>(HierarchicalToken.class)
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(0))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(1).setLowercase())) // "red"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(2))) // ">"
        .add(new HierarchicalToken(TokenType.MARKUP__COMMENT, text.subView(3)))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(4))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(5).setLowercase())) // "aqua"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(6))) // ">"
    );
  }

  @Test
  public void shouldTokenizeComplexInputHierarchicallyAndSequentially() {
    TextWithSubViews text = new TextWithSubViews(
      //  v- First subview-index within this line
      /*   0 */ "`<´`gray´`>´",
      /*   3 */ "  ``Currently,´` ´`the´` ´`following´` ´`players´` ´`are´` ´`online:´´`<´`space´`/´`>´",
      /*  19 */ "  `<´`red´",
      /*  21 */ "    `*´`for´`-´`player_name´`=´``\"´`player_names´`\"´´",
      /*  30 */ "    `*´`if´`=´``\"´`player_name´` ´`neq´` ´`'Steve'´`\"´´",
      /*  41 */ "    `*´`let´`-´`position_number´`=´``\"´` ´`loop´`.´`index´` ´`+´` ´`1´`\"´´",
      /*  57 */ "    `*´`for-separator´`=´`{´`<´`gray´`>´``,´` ´´`}´",
      /*  68 */ "  `>´",
      /*  69 */ "    `<´`run-command´",
      /*  71 */ "      `[´`value´`]´`=´``\"´``'/tp´` ´`'´´` ´`+´` ´`player_name´`\"´´",
      /*  86 */ "    `>´`#´``{´`position_number´`}´´` ´``{´` ´` ´`player_name´` ´`}´´`<´`/´`>´",
      /* 103 */ "  `<´`/´`>´",
      /* 106 */ "`<´`/´`>´"
    );

    InputView rootView = InputView.of(text.text);

    makeSequenceCase(
      rootView,
      new ListBuilder<>(HierarchicalToken.class)
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(0))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(1).setLowercase())) // "gray"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(2))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(2), list))
        .add(
          new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.subView(3).setBuildFlags(SubstringFlag.INNER_TEXT)) // "Currently, the following players are online:"
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(5))) // " "
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(7))) // " "
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(9))) // " "
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(11))) // " "
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(13))) // " "
        )
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(15))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(16).setLowercase())) // "space"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(17))) // "/"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(18))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(18), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(19))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(20).setLowercase())) // "red"
        .with(list -> addWhitespace(rootView, text.subView(20), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(21))) // "*"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(22).setLowercase())) // "for"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.subView(23).setLowercase())) // "-"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__BINDING, text.subView(24).setLowercase())) // "player_name"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(25))) // "="
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.subView(26)) // "\"player_names\""
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(28))) // "player_names"
        )
        .with(list -> addWhitespace(rootView, text.subView(26), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(30))) // "*"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(31).setLowercase())) // "if"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(32))) // "="
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.subView(33)) // "\"player_name != 'Steve'\""
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(35))) // "player_name"
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(36))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__NAMED_INFIX_OPERATOR, text.subView(37))) // "!="
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(38))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__STRING, text.subView(39))) // "'Steve'"
        )
        .with(list -> addWhitespace(rootView, text.subView(33), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(41))) // "*"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(42).setLowercase())) // "let"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.subView(43).setLowercase())) // "-"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__BINDING, text.subView(44).setLowercase())) // "position_number"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(45))) // "="
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.subView(46)) // "\" loop.index + 1\""
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(48))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(49))) // "loop"
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, text.subView(50))) // "."
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(51))) // "index"
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(52))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, text.subView(53))) // "+"
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(54))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__NUMBER, text.subView(55))) // "1"
        )
        .with(list -> addWhitespace(rootView, text.subView(46), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(57))) // "*"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(58).setLowercase())) // "for-separator"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(59))) // "="
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.subView(60))) // "{"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(61))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(62).setLowercase())) // "gray"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(63))) // ">"
        .add(
          new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.subView(64).setBuildFlags(SubstringFlag.LAST_TEXT)) // ", "
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(66))) // " "
        )
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.subView(67))) // "}"
        .with(list -> addWhitespace(rootView, text.subView(67), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(68))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(68), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(69))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(70).setLowercase())) // "run-command"
        .with(list -> addWhitespace(rootView, text.subView(70), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.subView(71))) // "["
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, text.subView(72).setLowercase())) // "value"
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.subView(73))) // "]"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(74))) // "="
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.subView(75)) // "\"'/tp ' + player_name\""
            .addChild(
              new HierarchicalToken(TokenType.EXPRESSION__STRING, text.subView(77)) // "'/tp '"
                .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(79))) // " "
            )
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(81))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, text.subView(82))) // "+"
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(83))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(84))) // "player_name"
        )
        .with(list -> addWhitespace(rootView, text.subView(75), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(86))) // ">"
        .add(new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.subView(87).setBuildFlags(SubstringFlag.INNER_TEXT))) // "#"
        .add(
          new HierarchicalToken(TokenType.ANY__INTERPOLATION, text.subView(88)) // "{position_number}"
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(90))) // "position_number"
        )
        .add(
          new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.subView(92).setBuildFlags(SubstringFlag.INNER_TEXT))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(92).buildSubViewRelative(0, 1)))
        ) // " "
        .add(
          new HierarchicalToken(TokenType.ANY__INTERPOLATION, text.subView(93)) // "{  player_name }
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(95))) // " "
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(96))) // " "
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(97))) // "player_name"
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.subView(98))) // " "
        )
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(100))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(101))) // "/"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(102))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(102), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(103))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(104))) // "/"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(105))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(104), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(106))) // "<"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(107))) // "/"
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(108))) // ">"
      ,
      new ListBuilder<>(Token.class)
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(0))) // "<"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(1).setLowercase())) // "gray"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(2))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(2), list))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(4))) // "Currently,"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(5))) // " "
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(6))) // "the"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(7))) // " "
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(8))) // "following"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(9))) // " "
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(10))) // "players"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(11))) // " "
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(12))) // "are"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(13))) // " "
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(14))) // "online:"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(15))) // "<"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(16).setLowercase())) // "space"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(17))) // "/"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(18))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(18), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(19))) // "<"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(20).setLowercase())) // "red"
        .with(list -> addWhitespace(rootView, text.subView(20), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(21))) // "*"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(22).setLowercase())) // "for"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.subView(23).setLowercase())) // "-"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__BINDING, text.subView(24).setLowercase())) // "player_name"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(25))) // "="
        .add(new Token(TokenType.MARKUP__STRING, text.subView(27))) // "\""
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(28))) // "player_names"
        .add(new Token(TokenType.MARKUP__STRING, text.subView(29))) // "\""
        .with(list -> addWhitespace(rootView, text.subView(29), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(30))) // "*"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(31).setLowercase())) // "if"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(32))) // "="
        .add(new Token(TokenType.MARKUP__STRING, text.subView(34))) // "\""
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(35))) // "player_name"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(36))) // " "
        .add(new Token(TokenType.EXPRESSION__NAMED_INFIX_OPERATOR, text.subView(37))) // "!="
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(38))) // " "
        .add(new Token(TokenType.EXPRESSION__STRING, text.subView(39))) // "'Steve'"
        .add(new Token(TokenType.MARKUP__STRING, text.subView(40))) // "\""
        .with(list -> addWhitespace(rootView, text.subView(40), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(41))) // "*"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(42).setLowercase())) // "let"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.subView(43).setLowercase())) // "-"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__BINDING, text.subView(44).setLowercase())) // "position_number"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(45))) // "="
        .add(new Token(TokenType.MARKUP__STRING, text.subView(47))) // "\""
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(48))) // " "
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(49))) // "loop"
        .add(new Token(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, text.subView(50))) // "."
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(51))) // "index"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(52))) // " "
        .add(new Token(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, text.subView(53))) // "+"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(54))) // " "
        .add(new Token(TokenType.EXPRESSION__NUMBER, text.subView(55))) // "1"
        .add(new Token(TokenType.MARKUP__STRING, text.subView(56))) // "\""
        .with(list -> addWhitespace(rootView, text.subView(56), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.subView(57))) // "*"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.subView(58).setLowercase())) // "for-separator"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(59))) // "="
        .add(new Token(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.subView(60))) // "{"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(61))) // "<"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(62))) // "gray"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(63))) // ">"
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(65))) // ","
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(66))) // " "
        .add(new Token(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.subView(67))) // "}"
        .with(list -> addWhitespace(rootView, text.subView(67), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(68))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(68), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(69))) // "<"
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.subView(70))) // "run-command"
        .with(list -> addWhitespace(rootView, text.subView(70), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.subView(71))) // "["
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, text.subView(72).setLowercase())) // "value"
        .add(new Token(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.subView(73))) // "]"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.subView(74))) // "="
        .add(new Token(TokenType.MARKUP__STRING, text.subView(76))) // "\""
        .add(new Token(TokenType.EXPRESSION__STRING, text.subView(78))) // "'/tp
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(79))) // " "
        .add(new Token(TokenType.EXPRESSION__STRING, text.subView(80))) // "'"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(81))) // " "
        .add(new Token(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, text.subView(82))) // "+"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(83))) // " "
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(84))) // "player_name"
        .add(new Token(TokenType.MARKUP__STRING, text.subView(85))) // "\""
        .with(list -> addWhitespace(rootView, text.subView(85), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(86))) // ">"
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.subView(87))) // "#"
        .add(new Token(TokenType.ANY__INTERPOLATION, text.subView(89))) // "{"
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(90))) // "position_number"
        .add(new Token(TokenType.ANY__INTERPOLATION, text.subView(91))) // "}"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(92).buildSubViewRelative(0, 1))) // " "
        .add(new Token(TokenType.ANY__INTERPOLATION, text.subView(94))) // "{"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(95))) // " "
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(96))) // " "
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.subView(97))) // "player_name"
        .add(new Token(TokenType.ANY__WHITESPACE, text.subView(98))) // " "
        .add(new Token(TokenType.ANY__INTERPOLATION, text.subView(99))) // "}"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(100))) // "<"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(101))) // "/"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(102))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(102), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(103))) // "<"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(104))) // "/"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(105))) // ">"
        .with(list -> addWhitespace(rootView, text.subView(105), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(106))) // "<"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(107))) // "/"
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.subView(108))) // ">"
    );
  }

  private void addWhitespace(InputView rootView, InputView predecessor, ListBuilder<? extends Token> output) {
    int textLength = rootView.contents.length();
    boolean foundFirstWhitespace = false;

    for (int charIndex = predecessor.endExclusive; charIndex < textLength; ++charIndex) {
      char currentChar = rootView.contents.charAt(charIndex);

      if (!Character.isWhitespace(currentChar)) {
        if (!foundFirstWhitespace)
          continue;

        break;
      }

      foundFirstWhitespace = true;

      InputView charView = rootView.buildSubViewAbsolute(charIndex, charIndex + 1);

      if (output.type == HierarchicalToken.class)
        output.add(new HierarchicalToken(TokenType.ANY__WHITESPACE, charView));

      else if (output.type == Token.class)
        output.add(new Token(TokenType.ANY__WHITESPACE, charView));

      else
        throw new IllegalStateException("Unknown token-type: " + output.type);
    }
  }

  private void makeSequenceCase(
    InputView rootView,
    ListBuilder<HierarchicalToken> expectedHierarchicalTokens,
    ListBuilder<Token> expectedSequenceTokens
  ) {
    List<HierarchicalToken> actualHierarchicalTokens = makeHierarchicalCase(rootView, expectedHierarchicalTokens);

    List<Token> actualSequenceTokens = new ArrayList<>();

    HierarchicalToken.toSequence(actualHierarchicalTokens, (type, value) -> {
      actualSequenceTokens.add(new Token(type, value));
    });

    Assertions.assertEquals(Jsonifier.jsonify(expectedSequenceTokens.getResult()), Jsonifier.jsonify(actualSequenceTokens));
  }

  private List<HierarchicalToken> makeHierarchicalCase(InputView rootView, ListBuilder<HierarchicalToken> expectedHierarchicalTokens) {
    TokenOutput tokenOutput = new TokenOutput(EnumSet.noneOf(OutputFlag.class));

    try {
      MarkupParser.parse(rootView, BuiltInTagRegistry.INSTANCE, tokenOutput);
    } catch (MarkupParseException e) {
      for (String line : e.makeErrorScreen())
        System.out.println(line);

      Assertions.fail("Expected there to not be an error");
    }

    List<HierarchicalToken> actualHierarchicalTokens = tokenOutput.getResult();

    Assertions.assertEquals(Jsonifier.jsonify(expectedHierarchicalTokens.getResult()), Jsonifier.jsonify(actualHierarchicalTokens));

    return actualHierarchicalTokens;
  }
}
