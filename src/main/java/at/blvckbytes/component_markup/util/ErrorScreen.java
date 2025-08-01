/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

import java.util.ArrayList;
import java.util.List;

public class ErrorScreen {

  public static List<String> make(StringView view, String message) {
    return make(view.contents, view.startInclusive, message);
  }

  public static List<String> make(String contents, int position, String message) {
    List<String> result = new ArrayList<>();

    int inputLength = contents.length();
    int lineCounter = 1;

    for (int index = 0; index < inputLength; ++index) {
      if (contents.charAt(index) == '\n')
        ++lineCounter;
    }

    int maxLineNumberDigits = (lineCounter + 9) / 10;
    int nextLineNumber = 1;
    int lineBegin = 0;

    for (int index = 0; index < inputLength; ++index) {
      char currentChar = contents.charAt(index);

      if (currentChar == '\r')
        continue;

      boolean isLastChar = index == inputLength - 1;

      if (currentChar == '\n' || isLastChar) {
        if (isLastChar && currentChar != '\n')
          ++index;

        String lineNumber = padLeft(nextLineNumber++, maxLineNumberDigits) + ": ";
        String lineContents = contents.substring(lineBegin, index);

        result.add(lineNumber + lineContents);

        if (position >= lineBegin && position < index) {
          int lineRelativeOffset = position - lineBegin;
          int charCountUntilTargetChar = lineRelativeOffset == 0 ? 0 : lineRelativeOffset + 1;
          int spacerLength = (lineNumber.length() + charCountUntilTargetChar) - 1;

          result.add(makeLine(spacerLength).append('^').toString());
          result.add("Error: " + message);
        }

        lineBegin = index + 1;
      }
    }

    return result;
  }

  private static StringBuilder makeLine(int count) {
    if (count <= 0)
      return new StringBuilder(0);

    StringBuilder result = new StringBuilder(count);

    for (int i = 0; i < count; ++i)
      result.append('-');

    return result;
  }

  private static String padLeft(int number, int width) {
    String numberString = Integer.toString(number);

    int pad = width - numberString.length();

    if (pad <= 0)
      return numberString;

    StringBuilder result = new StringBuilder(width);

    for (int i = 0; i < pad; i++)
      result.append('0');

    result.append(numberString);

    return result.toString();
  }
}
