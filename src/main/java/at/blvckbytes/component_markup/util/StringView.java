package at.blvckbytes.component_markup.util;

import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class StringView {

  public static final StringView EMPTY = new StringView("", new BitFlagArray(1), false, 0, 0);

  @JsonifyIgnore
  public final String contents;

  private final BitFlagArray removeIndices;

  private boolean lowercase;

  public final int startInclusive;
  public final int endExclusive;

  private int charIndex;

  private @Nullable EnumSet<SubstringFlag> buildFlags;

  @JsonifyIgnore
  private @Nullable String buildStringCache;

  private StringView(
    String contents,
    BitFlagArray removeIndices,
    boolean lowercase,
    int startInclusive,
    int endExclusive
  ) {
    this.contents = contents;
    this.removeIndices = removeIndices;
    this.lowercase = lowercase;

    this.startInclusive = startInclusive;
    this.endExclusive = endExclusive;

    if (startInclusive < 0)
      throw new IllegalStateException("Start-inclusive cannot be less than zero");

    // Allow for empty views
    if (startInclusive != 0 && startInclusive >= contents.length())
      throw new IllegalStateException("Start-inclusive cannot be greater than or equal to " + contents.length());

    if (endExclusive > contents.length())
      throw new IllegalStateException("End-exclusive cannot be greater than " + contents.length());

    if (endExclusive < startInclusive)
      throw new IllegalStateException("The end-exclusive-index cannot lie before or at the start-inclusive-index");

    charIndex = startInclusive - 1;
  }

  public StringView setLowercase() {
    lowercase = true;
    return this;
  }

  public StringView setBuildFlags(EnumSet<SubstringFlag> flags) {
    if (flags == null)
      throw new IllegalStateException("Provided illegal null-value for build-flags");

    if (buildFlags != null)
      throw new IllegalStateException("Build-flags were already set");

    this.buildFlags = flags;
    return this;
  }

  public static StringView of(String contents) {
    return new StringView(contents, new BitFlagArray(contents.length()), false, 0, contents.length());
  }

  public StringView buildSubViewRelative(int startInclusive) {
    return buildSubViewAbsolute(
      startInclusive + (startInclusive < 0 ? this.endExclusive : this.startInclusive),
      endExclusive
    );
  }

  public StringView buildSubViewRelative(int startInclusive, int endExclusive) {
    return buildSubViewAbsolute(
      startInclusive + (startInclusive < 0 ? this.endExclusive : this.startInclusive),
      endExclusive + (endExclusive < 0 ? this.endExclusive : this.startInclusive)
    );
  }

  public StringView buildSubViewAbsolute(int startInclusive, int endExclusive) {
    if (startInclusive < this.startInclusive || startInclusive >= this.endExclusive)
      throw new IllegalStateException("Start-inclusive " + startInclusive + " out of this view's range: [" + this.startInclusive + ";" + this.endExclusive + ")");

    if (endExclusive < this.startInclusive || endExclusive > this.endExclusive)
      throw new IllegalStateException("End-exclusive " + endExclusive + " out of this view's range: [" + this.startInclusive + ";" + this.endExclusive + "]");

    if (endExclusive < startInclusive)
      throw new IllegalStateException("End " + endExclusive + " lies before start " + startInclusive);

    return new StringView(contents, removeIndices, lowercase, startInclusive, endExclusive);
  }

  public void addIndexToBeRemoved(int index) {
    if (index < startInclusive || index >= endExclusive)
      throw new IllegalStateException("Index " + index + " out of this view's range: [" + startInclusive + ";" + endExclusive + ")");

    removeIndices.set(index);
  }

  public char nextChar() {
    if (charIndex >= endExclusive - 1)
      return 0;

    return contents.charAt(++charIndex);
  }

  public char priorChar(int offset) {
    if (offset < 0)
      throw new IllegalStateException("Do not provide negative offsets on priorChar()");

    if (charIndex < offset)
      return 0;

    return contents.charAt(charIndex - offset);
  }

  public char peekChar(int offset) {
    if (offset < 0)
      throw new IllegalStateException("Do not provide negative offsets on peekChar()");

    int targetIndex = charIndex + 1 + offset;

    if (targetIndex > endExclusive - 1)
      return 0;

    return contents.charAt(targetIndex);
  }

  @JsonifyGetter
  public String buildString() {
    if (buildStringCache != null)
      return buildStringCache;

    char[] result = new char[length()];
    int nextResultIndex = 0;

    boolean doIgnoreWhitespace = false;

    for (int inputIndex = startInclusive; inputIndex < endExclusive; ++inputIndex) {
      if (removeIndices.get(inputIndex))
        continue;

      char currentChar = contents.charAt(inputIndex);

      if (currentChar == '\n' && buildFlags != null && buildFlags.contains(SubstringFlag.REMOVE_NEWLINES_INDENT)) {
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

      if (currentChar == ' ' && buildFlags != null && buildFlags.contains(SubstringFlag.REMOVE_LEADING_SPACE) && nextResultIndex == 0) {
        doIgnoreWhitespace = true;
        continue;
      }

      if (lowercase)
        currentChar = Character.toLowerCase(currentChar);

      result[nextResultIndex++] = currentChar;
    }

    if (buildFlags != null && buildFlags.contains(SubstringFlag.REMOVE_TRAILING_SPACE)) {
      while (nextResultIndex > 0 && result[nextResultIndex - 1] == ' ')
        --nextResultIndex;
    }

    return buildStringCache = new String(result, 0, nextResultIndex);
  }

  public void consumeWhitespace(@Nullable TokenOutput tokenOutput) {
    while (Character.isWhitespace(peekChar(0))) {
      nextChar();

      if (tokenOutput != null)
        tokenOutput.emitCharToken(charIndex, TokenType.ANY__WHITESPACE);
    }
  }

  public int getPosition() {
    return charIndex;
  }

  public boolean startsWith(@NotNull String value, boolean ignoreCase) {
    return _getFirstMismatchIndex(value, ignoreCase) < 0;
  }

  public boolean contentEquals(@NotNull String value, boolean ignoreCase) {
    if (length() != value.length())
      return false;

    return _getFirstMismatchIndex(value, ignoreCase) < 0;
  }

  private int _getFirstMismatchIndex(@NotNull String value, boolean ignoreCase) {
    int valueIndex = 0;

    if (length() < value.length())
      return endExclusive;

    for (int index = startInclusive; index < endExclusive; ++index) {
      if (removeIndices.get(index))
        continue;

      if (valueIndex == value.length())
        return -1;

      char valueChar = value.charAt(valueIndex++);
      char contentsChar = contents.charAt(index);

      if (ignoreCase)
        valueChar = Character.toLowerCase(valueChar);

      if (ignoreCase || lowercase)
        contentsChar = Character.toLowerCase(contentsChar);

      if (valueChar != contentsChar)
        return index;
    }

    return -1;
  }

  public char nthChar(int index) {
    int targetIndex = startInclusive + index;

    if (targetIndex >= endExclusive)
      return 0;

    return contents.charAt(targetIndex);
  }

  public int length() {
    return endExclusive - startInclusive;
  }
}
