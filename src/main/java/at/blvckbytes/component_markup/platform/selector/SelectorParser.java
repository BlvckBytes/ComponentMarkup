/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.platform.selector.argument.ArgumentEntry;
import at.blvckbytes.component_markup.platform.selector.argument.ArgumentName;
import at.blvckbytes.component_markup.platform.selector.argument.ArgumentValue;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectorParser {

  public static @NotNull TargetSelector parse(StringView input) {
    input.consumeWhitespace(null);

    if (input.nextChar() != '@')
      throw new SelectorParseException(input, SelectorParseError.MISSING_AT_SYMBOL);

    char peekedChar;

    if ((peekedChar = input.peekChar(0)) == 0 || Character.isWhitespace(peekedChar))
      throw new SelectorParseException(input, SelectorParseError.MISSING_TARGET_TYPE);

    input.nextChar();

    int typeBegin = input.getPosition();

    char upcomingChar;

    while ((upcomingChar = input.peekChar(0)) != 0 && upcomingChar != '[' && !Character.isWhitespace(upcomingChar))
      input.nextChar();

    StringView rawTarget = input.buildSubViewAbsolute(typeBegin, input.getPosition() + 1);
    TargetType target = TargetType.ofName(rawTarget);

    if (target == null)
      throw new SelectorParseException(input, typeBegin, SelectorParseError.UNKNOWN_TARGET_TYPE, rawTarget.buildString());

    input.consumeWhitespace(null);

    if (input.peekChar(0) == 0)
      return new TargetSelector(target, rawTarget, Collections.emptyList());

    if (input.nextChar() != '[')
      throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENTS_OPENING_BRACKET);

    List<ArgumentEntry> arguments = parseArguments(input);

    if (input.nextChar() != ']')
      throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENTS_CLOSING_BRACKET);

    return new TargetSelector(target, rawTarget, arguments);
  }

  private static List<ArgumentEntry> parseArguments(StringView input) {
    List<ArgumentEntry> result = new ArrayList<>();

    while (input.peekChar(0) != 0) {
      input.consumeWhitespace(null);

      char firstNameChar = input.peekChar(0);

      if (firstNameChar == 0 || firstNameChar == ']')
        return result;

      input.nextChar();

      int nameBegin = input.getPosition();

      if (firstNameChar == '=')
        throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENT_NAME);

      if (firstNameChar == ',')
        throw new SelectorParseException(input, SelectorParseError.DANGLING_ARGUMENT_SEPARATOR);

      char peekedChar;

      while ((peekedChar = input.peekChar(0)) >= 'a' && peekedChar <= 'z' || peekedChar >= 'A' && peekedChar <= 'Z')
        input.nextChar();

      StringView rawName = input.buildSubViewAbsolute(nameBegin, input.getPosition() + 1);

      ArgumentName name = ArgumentName.ofName(rawName);

      if (name == null)
        throw new SelectorParseException(input, nameBegin, SelectorParseError.UNKNOWN_ARGUMENT_NAME, rawName.buildString());

      input.consumeWhitespace(null);

      if (input.nextChar() != '=')
        throw new SelectorParseException(input, SelectorParseError.MISSING_EQUALS_SIGN, rawName.buildString());

      result.add(new ArgumentEntry(name, rawName, parseArgumentValue(name)));

      input.consumeWhitespace(null);

      if (input.peekChar(0) == ']')
        return result;

      if (input.nextChar() != ',')
        throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENT_SEPARATOR, rawName.buildString());
    }

    return result;
  }

  private static ArgumentValue parseArgumentValue(ArgumentName name) {
    // TODO: Implement me! :)
    throw new UnsupportedOperationException();
  }
}
