package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.test_utils.ListBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TokenOutputTests {

  @Test
  public void shouldTokenizeComplexInputHierarchicallyAndSequentially() {
    TextWithAnchors text = new TextWithAnchors(
      "@<@gray@>",
      "  @Currently,@ the@ following@ players@ are@ online:@<@space@/@>",
      "  @<@red",
      "    @*@for@-@player_name@=@\"@player_names@\"",
      "    @*@if@=@\"@player_name@ @!=@ @'Steve'@\"",
      "    @*@let@-@position_number@=@\"@ @loop@.@index@ @+@ @1@\"",
      "    @*@for-separator@=@{@<@gray@>@,@ @}",
      "  @>",
      "    @<@run-command",
      "      @[@value@]@=@\"@'/tp@ '@ @+@ @player_name@\"",
      "    @>@\\#@{@position_number@}@ @{# # @player_name# @}@<@/@>",
      "  @<@/@>",
      "@<@/@>"
    );

    makeCase(
      text,
      new ListBuilder<>(HierarchicalToken.class)
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(0), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(1), "gray"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(2), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(2), list))
        .add(
          new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(3), "Currently, the following players are online:")
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(4), " "))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(5), " "))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(6), " "))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(7), " "))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(8), " "))
        )
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(9), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(10), "space"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(11), "/"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(12), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(12), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(13), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(14), "red"))
        .with(list -> addWhitespace(text, text.anchorIndex(14), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(15), "*"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(16), "for"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.anchorIndex(17), "-"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__BINDING, text.anchorIndex(18), "player_name"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(19), "="))
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.anchorIndex(20), "\"player_names\"")
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(21), "player_names"))
        )
        // 22 - closing-quote
        .with(list -> addWhitespace(text, text.anchorIndex(22), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(23), "*"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(24), "if"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(25), "="))
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.anchorIndex(26), "\"player_name != 'Steve'\"")
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(27), "player_name"))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(28), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(29), "!="))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(30), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__STRING, text.anchorIndex(31), "'Steve'"))
        )
        // 32 - closing-quote
        .with(list -> addWhitespace(text, text.anchorIndex(32), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(33), "*"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(34), "let"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.anchorIndex(35), "-"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__BINDING, text.anchorIndex(36), "position_number"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(37), "="))
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.anchorIndex(38), "\" loop.index + 1\"")
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(39), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(40), "loop"))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(41), "."))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(42), "index"))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(43), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(44), "+"))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(45), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__NUMBER, text.anchorIndex(46), "1"))
        )
        // 47 - closing-quote
        .with(list -> addWhitespace(text, text.anchorIndex(47), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(48), "*"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(49), "for-separator"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(50), "="))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.anchorIndex(51), "{"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(52), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(53), "gray"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(54), ">"))
        .add(
          new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(55), ", ")
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(56), " "))
        )
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.anchorIndex(57), "}"))
        .with(list -> addWhitespace(text, text.anchorIndex(57), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(58), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(58), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(59), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(60), "run-command"))
        .with(list -> addWhitespace(text, text.anchorIndex(60), list))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.anchorIndex(61), "["))
        .add(new HierarchicalToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, text.anchorIndex(62), "value"))
        .add(new HierarchicalToken(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.anchorIndex(63), "]"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(64), "="))
        .add(
          new HierarchicalToken(TokenType.MARKUP__STRING, text.anchorIndex(65), "\"'/tp ' + player_name\"")
            .addChild(
              new HierarchicalToken(TokenType.EXPRESSION__STRING, text.anchorIndex(66), "'/tp '")
                .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(67), " "))
            )
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(68), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(69), "+"))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.anchorIndex(70), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(71), "player_name"))
        )
        // 72 - closing-quote
        .with(list -> addWhitespace(text, text.anchorIndex(72), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(73), ">"))
        .add(new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(74), "#"))
        .add(
          new HierarchicalToken(TokenType.MARKUP__INTERPOLATION, text.anchorIndex(75), "{position_number}")
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(76), "position_number"))
        )
        // 77 - closing-bracket
        .add(new HierarchicalToken(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(78), " "))
        .add(
          new HierarchicalToken(TokenType.MARKUP__INTERPOLATION, text.anchorIndex(79), "{  player_name }")
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.auxAnchorIndex(0), " "))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.auxAnchorIndex(1), " "))
            .addChild(new HierarchicalToken(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(80), "player_name"))
            .addChild(new HierarchicalToken(TokenType.ANY__WHITESPACE, text.auxAnchorIndex(2), " "))
        )
        // 81 - closing-bracket
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(82), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(83), "/"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(84), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(84), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(85), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(86), "/"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(87), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(87), list))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(88), "<"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(89), "/"))
        .add(new HierarchicalToken(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(90), ">"))
      ,
      new ListBuilder<>(Token.class)
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(0), "<"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(1), "gray"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(2), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(2), list))
        // Currently, the following players are online:
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(3), "Currently,"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(4), " "))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(4) + 1, "the"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(5), " "))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(5) + 1, "following"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(6), " "))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(6) + 1, "players"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(7), " "))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(7) + 1, "are"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(8), " "))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(8) + 1, "online:"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(9), "<"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(10), "space"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(11), "/"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(12), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(12), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(13), "<"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(14), "red"))
        .with(list -> addWhitespace(text, text.anchorIndex(14), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(15), "*"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(16), "for"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.anchorIndex(17), "-"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__BINDING, text.anchorIndex(18), "player_name"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(19), "="))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(20), "\""))
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(21), "player_names"))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(22), "\""))
        .with(list -> addWhitespace(text, text.anchorIndex(22), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(23), "*"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(24), "if"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(25), "="))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(26), "\""))
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(27), "player_name"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(28), " "))
        .add(new Token(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(29), "!="))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(30), " "))
        .add(new Token(TokenType.EXPRESSION__STRING, text.anchorIndex(31), "'Steve'"))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(32), "\""))
        .with(list -> addWhitespace(text, text.anchorIndex(32), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(33), "*"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(34), "let"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, text.anchorIndex(35), "-"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__BINDING, text.anchorIndex(36), "position_number"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(37), "="))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(38), "\""))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(39), " "))
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(40), "loop"))
        .add(new Token(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(41), "."))
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(42), "index"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(43), " "))
        .add(new Token(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(44), "+"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(45), " "))
        .add(new Token(TokenType.EXPRESSION__NUMBER, text.anchorIndex(46), "1"))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(47), "\""))
        .with(list -> addWhitespace(text, text.anchorIndex(47), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, text.anchorIndex(48), "*"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, text.anchorIndex(49), "for-separator"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(50), "="))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.anchorIndex(51), "{"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(52), "<"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(53), "gray"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(54), ">"))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(55), ","))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(56), " "))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__SUBTREE, text.anchorIndex(57), "}"))
        .with(list -> addWhitespace(text, text.anchorIndex(57), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(58), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(58), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(59), "<"))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__TAG, text.anchorIndex(60), "run-command"))
        .with(list -> addWhitespace(text, text.anchorIndex(60), list))
        .add(new Token(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.anchorIndex(61), "["))
        .add(new Token(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, text.anchorIndex(62), "value"))
        .add(new Token(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, text.anchorIndex(63), "]"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__EQUALS, text.anchorIndex(64), "="))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(65), "\""))
        .add(new Token(TokenType.EXPRESSION__STRING, text.anchorIndex(66), "'/tp"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(67), " "))
        .add(new Token(TokenType.EXPRESSION__STRING, text.anchorIndex(67) + 1, "'"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(68), " "))
        .add(new Token(TokenType.EXPRESSION__OPERATOR__ANY, text.anchorIndex(69), "+"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.anchorIndex(70), " "))
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(71), "player_name"))
        .add(new Token(TokenType.MARKUP__STRING, text.anchorIndex(72), "\""))
        .with(list -> addWhitespace(text, text.anchorIndex(72), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(73), ">"))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(74), "#"))
        .add(new Token(TokenType.MARKUP__INTERPOLATION, text.anchorIndex(75), "{"))
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(76), "position_number"))
        .add(new Token(TokenType.MARKUP__INTERPOLATION, text.anchorIndex(77), "}"))
        .add(new Token(TokenType.MARKUP__PLAIN_TEXT, text.anchorIndex(78), " "))
        .add(new Token(TokenType.MARKUP__INTERPOLATION, text.anchorIndex(79), "{"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.auxAnchorIndex(0), " "))
        .add(new Token(TokenType.ANY__WHITESPACE, text.auxAnchorIndex(1), " "))
        .add(new Token(TokenType.EXPRESSION__IDENTIFIER_ANY, text.anchorIndex(80), "player_name"))
        .add(new Token(TokenType.ANY__WHITESPACE, text.auxAnchorIndex(2), " "))
        .add(new Token(TokenType.MARKUP__INTERPOLATION, text.anchorIndex(81), "}"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(82), "<"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(83), "/"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(84), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(84), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(85), "<"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(86), "/"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(87), ">"))
        .with(list -> addWhitespace(text, text.anchorIndex(87), list))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(88), "<"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(89), "/"))
        .add(new Token(TokenType.MARKUP__PUNCTUATION__TAG, text.anchorIndex(90), ">"))
    );
  }

  private void addWhitespace(TextWithAnchors input, int position, ListBuilder<? extends Token> output) {
    int textLength = input.text.length();
    boolean foundFirstWhitespace = false;

    for (int charIndex = position + 1; charIndex < textLength; ++charIndex) {
      char currentChar = input.text.charAt(charIndex);

      if (!Character.isWhitespace(currentChar)) {
        if (!foundFirstWhitespace)
          continue;

        break;
      }

      foundFirstWhitespace = true;

      if (output.type == HierarchicalToken.class)
        output.add(new HierarchicalToken(TokenType.ANY__WHITESPACE, charIndex, currentChar));

      else if (output.type == Token.class)
        output.add(new Token(TokenType.ANY__WHITESPACE, charIndex, currentChar));

      else
        throw new IllegalStateException("Unknown token-type: " + output.type);
    }
  }

  private void makeCase(
    TextWithAnchors input,
    ListBuilder<HierarchicalToken> expectedHierarchicalTokens,
    ListBuilder<Token> expectedSequenceTokens
  ) {
    TokenOutput tokenOutput = new TokenOutput(EnumSet.noneOf(OutputFlag.class));

    try {
      MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE, tokenOutput);
    } catch (MarkupParseException e) {
      for (String line : e.makeErrorScreen())
        System.out.println(line);

      Assertions.fail("Expected there to not be an error");
    }

    List<HierarchicalToken> actualHierarchicalTokens = tokenOutput.getResult();

    Assertions.assertEquals(Jsonifier.jsonify(expectedHierarchicalTokens.getResult()), Jsonifier.jsonify(actualHierarchicalTokens));

    List<Token> actualSequenceTokens = new ArrayList<>();

    HierarchicalToken.toSequence(actualHierarchicalTokens, (type, beginIndex, value) -> {
      actualSequenceTokens.add(new Token(type, beginIndex, value));
    });

    Assertions.assertEquals(Jsonifier.jsonify(expectedSequenceTokens.getResult()), Jsonifier.jsonify(actualSequenceTokens));
  }
}
