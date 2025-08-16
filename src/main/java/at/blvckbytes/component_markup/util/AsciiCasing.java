/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

public class AsciiCasing {

  public static String upper(String input) {
    int inputLength = input.length();
    StringBuilder result = new StringBuilder(inputLength);

    for (int charIndex = 0; charIndex < inputLength; ++charIndex)
      result.append(upper(input.charAt(charIndex)));

    return result.toString();
  }

  public static char upper(char input) {
    if (input >= 'a' && input <= 'z')
      input -= 32;

    return input;
  }

  public static String lower(String input) {
    int inputLength = input.length();
    StringBuilder result = new StringBuilder(inputLength);

    for (int charIndex = 0; charIndex < inputLength; ++charIndex)
      result.append(lower(input.charAt(charIndex)));

    return result.toString();
  }

  public static char lower(char input) {
    if (input >= 'A' && input <= 'Z')
      input += 32;

    return input;
  }
}
