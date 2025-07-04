package at.blvckbytes.component_markup.util;

import java.util.EnumSet;

public class SubstringBuilder {

  private int startInclusive;
  private int endExclusive;

  private final String input;
  private final int[] removeIndices;
  private int nextRemoveIndicesIndex;

  public SubstringBuilder(String input) {
    this.removeIndices = new int[64];
    this.input = input;
    this.resetIndices();
  }

  private void resetIndices() {
    this.startInclusive = -1;
    this.endExclusive = 0;
    this.nextRemoveIndicesIndex = 0;
  }

  public boolean hasStartSet() {
    return this.startInclusive >= 0;
  }

  public boolean hasEndSet() {
    return this.endExclusive > 0;
  }

  public void setStartInclusive(int index) {
    this.resetIndices();
    this.startInclusive = index;
  }

  public void addIndexToBeRemoved(int index) {
    if (nextRemoveIndicesIndex == removeIndices.length)
      throw new IllegalStateException("Exhausted array of indices to be removed");

    removeIndices[nextRemoveIndicesIndex++] = index;
  }

  public void setEndExclusive(int index) {
    if (index == 0)
      throw new IllegalStateException("End-exclusive cannot be zero");

    this.endExclusive = index;
  }

  public String build(EnumSet<SubstringFlag> flags) {
    if (this.startInclusive < 0)
      throw new IllegalStateException("Cannot build a substring without a determined start");

    if (this.endExclusive == 0)
      throw new IllegalStateException("Cannot build a substring without a determined end");

    int substringLength = (endExclusive - startInclusive) - nextRemoveIndicesIndex;

    if (substringLength < 0)
      throw new IllegalStateException("There were more characters to be removed than the substring was in total length");

    char[] result = new char[substringLength];
    int nextResultIndex = 0;
    int lastMatchedRemoveIndicesIndex = 0;
    boolean doIgnoreWhitespace = false;

    inputLoop: for (int inputIndex = startInclusive; inputIndex < endExclusive; ++inputIndex) {
      // Remove-indices are strictly increasing, thus avoid looping useless slots
      for (int removeIndicesIndex = lastMatchedRemoveIndicesIndex; removeIndicesIndex < nextRemoveIndicesIndex; ++removeIndicesIndex) {
        if (removeIndices[removeIndicesIndex] == inputIndex) {
          lastMatchedRemoveIndicesIndex = removeIndicesIndex;
          continue inputLoop;
        }
      }

      char currentChar = input.charAt(inputIndex);

      if (currentChar == '\n' && flags.contains(SubstringFlag.REMOVE_NEWLINES_INDENT)) {
        while (nextResultIndex > 0 && result[nextResultIndex - 1] == ' ')
          --nextResultIndex;

        doIgnoreWhitespace = true;
        continue;
      }

      if (doIgnoreWhitespace) {
        if (Character.isWhitespace(currentChar))
          continue;

        doIgnoreWhitespace = false;
      }

      if (currentChar == ' ' && flags.contains(SubstringFlag.REMOVE_LEADING_SPACE) && nextResultIndex == 0) {
        doIgnoreWhitespace = true;
        continue;
      }

      result[nextResultIndex++] = currentChar;
    }

    if (flags.contains(SubstringFlag.REMOVE_TRAILING_SPACE)) {
      while (nextResultIndex > 0 && result[nextResultIndex - 1] == ' ')
        --nextResultIndex;
    }

    this.resetIndices();

    return new String(result, 0, nextResultIndex);
  }
}
