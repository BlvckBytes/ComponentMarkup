/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml;

public class InStringDetector {

  private long stackBits;
  private int stackSize;

  public void onEncounter(char c) {
    boolean isSingleQuote = c == '\'';

    if (!isSingleQuote && c != '"')
      return;

    long bitMask = 1;

    if (stackSize > 1)
      bitMask <<= stackSize - 1;

    if (stackSize == 0 || ((stackBits & bitMask) == 0) == isSingleQuote) {
      if (++stackSize != 1)
        bitMask <<= 1;

      // If that's exceeded, there's something horribly wrong with the input...
      if (stackSize == Long.SIZE + 1)
        throw new IllegalStateException("Maximum quote-stack-depth exceeded");

      if (isSingleQuote)
        stackBits |= bitMask;
      else
        stackBits &= ~bitMask;

      return;
    }

    --stackSize;
  }

  public boolean isInString() {
    return this.stackSize != 0;
  }

  public void reset() {
    this.stackSize = 0;
  }
}
