package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class AttributeName {

  public final StringView finalName;
  public final StringView fullName;
  private final EnumSet<AttributeFlag> flags;

  private AttributeName(StringView finalName, StringView fullName, EnumSet<AttributeFlag> flags) {
    this.finalName = finalName;
    this.fullName = fullName;
    this.flags = flags;
  }

  public boolean has(AttributeFlag flag) {
    return flags.contains(flag);
  }

  public static AttributeName parse(StringView attributeName, @Nullable TokenOutput tokenOutput, boolean allowsNegation) {
    StringView fullName = attributeName;
    EnumSet<AttributeFlag> flags = EnumSet.noneOf(AttributeFlag.class);

    int nameLength;

    while ((nameLength = attributeName.length()) > 0) {
      char firstChar = attributeName.nthChar(0);

      if (firstChar == '*' || firstChar == '+') {
        if (flags.contains(AttributeFlag.BINDING_MODE))
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.BRACKETED_INTRINSIC_ATTRIBUTE);

        if (flags.contains(AttributeFlag.INTRINSIC_LITERAL) ||flags.contains(AttributeFlag.INTRINSIC_EXPRESSION))
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.MULTIPLE_ATTRIBUTE_INTRINSIC_MARKERS);

        if (!flags.isEmpty())
          throw new IllegalStateException("Due to the grammar of attribute-operators, this case should be unreachable");

        AttributeFlag flag = firstChar == '*' ? AttributeFlag.INTRINSIC_EXPRESSION : AttributeFlag.INTRINSIC_LITERAL;

        if (tokenOutput != null) {
          if (flag == AttributeFlag.INTRINSIC_EXPRESSION)
            tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, attributeName.buildSubViewRelative(0, 1));
          else
            tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__INTRINSIC_LITERAL, attributeName.buildSubViewRelative(0, 1));
        }

        if (nameLength == 1)
          throw new MarkupParseException(fullName.startInclusive, MarkupParseError.EMPTY_ATTRIBUTE_NAME);

        flags.add(flag);
        attributeName = attributeName.buildSubViewRelative(1);
        continue;
      }

      boolean hasOpeningBracket = firstChar == '[';
      boolean hasClosingBracket = nameLength > 1 && attributeName.nthChar(nameLength - 1) == ']';

      if (hasOpeningBracket || hasClosingBracket) {
        if (!hasOpeningBracket || !hasClosingBracket)
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS);

        if (flags.contains(AttributeFlag.BINDING_MODE))
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.MULTIPLE_ATTRIBUTE_BRACKETS);

        if (!flags.isEmpty())
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.LATE_ATTRIBUTE_BRACKETS);

        if (tokenOutput != null) {
          tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, attributeName.buildSubViewRelative(0, 1));
          tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, attributeName.buildSubViewRelative(-1));
        }

        if (nameLength == 2)
          throw new MarkupParseException(fullName.startInclusive, MarkupParseError.EMPTY_ATTRIBUTE_NAME);

        flags.add(AttributeFlag.BINDING_MODE);
        attributeName = attributeName.buildSubViewRelative(1, -1);
        continue;
      }

      if (firstChar == '.') {
        if (attributeName.length() < 3 || attributeName.nthChar(1) != '.' || attributeName.nthChar(2) != '.')
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.MALFORMED_SPREAD_OPERATOR);

        if (flags.contains(AttributeFlag.SPREAD_MODE))
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.MULTIPLE_ATTRIBUTE_SPREADS);

        if (!flags.contains(AttributeFlag.BINDING_MODE))
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.SPREAD_DISALLOWED_ON_NON_BINDING, attributeName.buildString());

        if (tokenOutput != null)
          tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__SPREAD, attributeName.buildSubViewRelative(0, 3));

        if (nameLength == 3)
          throw new MarkupParseException(fullName.startInclusive, MarkupParseError.EMPTY_ATTRIBUTE_NAME);

        flags.add(AttributeFlag.SPREAD_MODE);
        attributeName = attributeName.buildSubViewRelative(3);
        continue;
      }

      if (firstChar == '!') {
        if (flags.contains(AttributeFlag.FLAG_NEGATION))
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.MULTIPLE_ATTRIBUTE_NEGATIONS);

        if (!allowsNegation)
          throw new MarkupParseException(attributeName.startInclusive, MarkupParseError.DISALLOWED_ATTRIBUTE_NEGATION);

        if (tokenOutput != null)
          tokenOutput.emitCharToken(attributeName.startInclusive, TokenType.MARKUP__OPERATOR__NEGATE);

        if (nameLength == 1)
          throw new MarkupParseException(fullName.startInclusive, MarkupParseError.EMPTY_ATTRIBUTE_NAME);

        flags.add(AttributeFlag.FLAG_NEGATION);
        attributeName = attributeName.buildSubViewRelative(1);
        continue;
      }

      break;
    }

    if (nameLength == 0)
      throw new MarkupParseException(fullName.startInclusive, MarkupParseError.EMPTY_ATTRIBUTE_NAME);

    attributeName.setLowercase();

    return new AttributeName(attributeName, fullName, flags);
  }
}
