package at.blvckbytes.component_markup.util;

import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class StringView {

  @JsonifyIgnore
  public final String contents;

  @JsonifyIgnore
  private final StringView rootView;

  private final BitFlagArray removeIndices;

  private boolean lowercase;

  public final StringPosition startInclusive;
  public final StringPosition endExclusive;

  private int charIndex;

  @JsonifyIgnore
  private char priorNextChar;
  private char currentChar;

  @JsonifyIgnore
  private @Nullable StringPosition subViewStart;

  private @Nullable EnumSet<SubstringFlag> buildFlags;

  @JsonifyIgnore
  private @Nullable String buildStringCache;

  private StringView(
    String contents,
    @Nullable StringView rootView,
    BitFlagArray removeIndices,
    boolean lowercase,
    @Nullable StringPosition startInclusive,
    @Nullable StringPosition endExclusive
  ) {
    this.contents = contents;
    this.removeIndices = removeIndices;
    this.lowercase = lowercase;

    if (rootView == null)
      rootView = this;

    this.rootView = rootView;

    if (startInclusive == null)
      startInclusive = new StringPosition(rootView, 0);

    if (endExclusive == null)
      endExclusive = new StringPosition(rootView, contents.length());

    this.startInclusive = startInclusive;
    this.endExclusive = endExclusive;

    if (startInclusive.charIndex < 0)
      throw new IllegalStateException("Start-inclusive cannot be less than zero");

    if (startInclusive.charIndex >= contents.length())
      throw new IllegalStateException("Start-inclusive cannot be greater than or equal to " + contents.length());

    if (endExclusive.charIndex > contents.length())
      throw new IllegalStateException("Start-inclusive cannot be greater than " + contents.length());

    if (endExclusive.charIndex <= startInclusive.charIndex)
      throw new IllegalStateException("The end-exclusive-index cannot lie before or at the start-inclusive-index");

    charIndex = startInclusive.charIndex - 1;
    priorNextChar = 0;
  }

  public void setLowercase() {
    lowercase = true;
  }

  public void setBuildFlags(EnumSet<SubstringFlag> flags) {
    if (flags == null)
      throw new IllegalStateException("Provided illegal null-value for build-flags");

    if (buildFlags != null)
      throw new IllegalStateException("Build-flags were already set");

    this.buildFlags = flags;
  }

  public static StringView of(String contents) {
    int contentsLength = contents.length();

    if (contentsLength == 0)
      throw new IllegalStateException("Cannot instantiate an empty view");

    return new StringView(contents, null, new BitFlagArray(contentsLength), false, null, null);
  }

  public StringView buildSubViewAbsolute(int start) {
    return buildSubViewAbsolute(start, endExclusive.charIndex);
  }

  public StringView buildSubViewRelative(int start) {
    if (start < 0)
      throw new IllegalStateException("Start " + start + " cannot be negative when in relative-mode");

    return buildSubViewAbsolute(startInclusive.charIndex + start, endExclusive.charIndex);
  }

  public StringView buildSubViewRelative(int startInclusive, int endInclusive) {
    if (startInclusive < 0)
      throw new IllegalStateException("Start " + startInclusive + " cannot be negative when in relative-mode");

    return buildSubViewAbsolute(
      this.startInclusive.charIndex + startInclusive,
      endInclusive + (endInclusive < 0 ? this.endExclusive.charIndex : this.startInclusive.charIndex + 1)
    );
  }

  public StringView buildSubViewAbsolute(int startInclusive, int endExclusive) {
    if (buildFlags != null && !buildFlags.isEmpty())
      throw new IllegalStateException("Do not create sub-views while special flags are active");

    if (startInclusive < this.startInclusive.charIndex || startInclusive >= this.endExclusive.charIndex)
      throw new IllegalStateException("Start-inclusive " + startInclusive + " out of this view's range: [" + this.startInclusive.charIndex + ";" + this.endExclusive.charIndex + ")");

    if (endExclusive < this.startInclusive.charIndex || endExclusive > this.endExclusive.charIndex)
      throw new IllegalStateException("End-exclusive " + endExclusive + " out of this view's range: [" + this.startInclusive.charIndex + ";" + this.endExclusive.charIndex + "]");

    if (endExclusive < startInclusive)
      throw new IllegalStateException("End " + endExclusive + " lies before start " + startInclusive);

    if (startInclusive == this.startInclusive.charIndex && endExclusive == this.endExclusive.charIndex)
      return this;

    return new StringView(contents, rootView, removeIndices, lowercase, new StringPosition(this, startInclusive), new StringPosition(this, endExclusive));
  }

  public StringView buildSubViewInclusive(StringPosition position) {
    if (buildFlags != null && !buildFlags.isEmpty())
      throw new IllegalStateException("Do not create sub-views while special flags are active");

    if (subViewStart == null)
      throw new IllegalStateException("Cannot build a sub-StringView without a determined start");

    StringView subView = new StringView(
      contents, rootView, removeIndices, lowercase, subViewStart,
      new StringPosition(position.rootView, position.charIndex + 1)
    );

    subViewStart = null;

    return subView;
  }

  public StringView buildSubViewInclusive(PositionMode mode) {
    return buildSubViewInclusive(getPosition(mode));
  }

  public int getCharIndex() {
    return Math.max(this.charIndex, 0);
  }

  public void addIndexToBeRemoved(int index) {
    if (index < startInclusive.charIndex || index >= endExclusive.charIndex)
      throw new IllegalStateException("Index " + index + " out of this view's range: [" + startInclusive.charIndex + ";" + endExclusive.charIndex + ")");

    removeIndices.set(index);
  }

  public char priorNextChar() {
    return priorNextChar;
  }

  public char nextChar() {
    if (hasReachedEnd())
      return 0;

    priorNextChar = currentChar;
    currentChar = contents.charAt(++charIndex);

    return currentChar;
  }

  private boolean hasReachedEnd() {
    if (charIndex > endExclusive.charIndex - 1)
      throw new IllegalStateException("The char-index should never exceed its length-dictated maximum");

    return charIndex == endExclusive.charIndex - 1;
  }

  public char peekChar() {
    if (hasReachedEnd())
      return 0;

    return contents.charAt(charIndex + 1);
  }

  public void clearSubViewStart() {
    if (subViewStart == null)
      throw new IllegalStateException("A sub-view's start-position was not set");

    subViewStart = null;
  }

  public void setSubViewStart(StringPosition position) {
    if (position == null)
      throw new IllegalStateException("provided non-null sub-view start-position");

    if (subViewStart != null)
      throw new IllegalStateException("A sub-view's start-position was already set");

    if (position.charIndex < startInclusive.charIndex || position.charIndex >= endExclusive.charIndex)
      throw new IllegalStateException("Index " + position.charIndex + " out of this view's range: [" + startInclusive.charIndex + ";" + endExclusive.charIndex + ")");

    subViewStart = position;
  }

  public @Nullable StringPosition getSubViewStart() {
    return subViewStart;
  }

  @JsonifyGetter
  public String buildString() {
    if (buildStringCache != null)
      return buildStringCache;

    char[] result = new char[length()];
    int nextResultIndex = 0;

    boolean doIgnoreWhitespace = false;

    for (int inputIndex = startInclusive.charIndex; inputIndex < endExclusive.charIndex; ++inputIndex) {
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

  public boolean consumeWhitespaceAndGetIfNewline(@Nullable TokenOutput tokenOutput) {
    boolean encounteredNewline = false;

    while (Character.isWhitespace(peekChar())) {
      encounteredNewline |= nextChar() == '\n';

      if (tokenOutput != null)
        tokenOutput.emitCharToken(getPosition(), TokenType.ANY__WHITESPACE);
    }

    return encounteredNewline;
  }

  public void restorePosition(StringPosition position) {
    if (position.charIndex < -1 || position.charIndex >= endExclusive.charIndex)
      throw new IllegalStateException("Position at " + position.charIndex + " out of this view's range: [" + -1 + ";" + endExclusive.charIndex + ")");

    if (position.charIndex != endExclusive.charIndex - 1)
      removeIndices.clearRange(position.charIndex + 1, endExclusive.charIndex - 1);

    charIndex = position.charIndex;

    if (charIndex < 0) {
      priorNextChar = 0;
      currentChar = 0;
    } else {
      priorNextChar = contents.charAt(charIndex);
      currentChar = contents.charAt(charIndex + 1);
    }
  }

  public StringPosition getPosition(PositionMode mode) {
    if (mode == PositionMode.CURRENT)
      return new StringPosition(this, charIndex);

    if (mode == PositionMode.PRIOR) {
      if (charIndex < 0)
        throw new IllegalStateException("No prior char available");

      return new StringPosition(this, charIndex - 1);
    }

    if (mode != PositionMode.NEXT)
      throw new IllegalStateException("Unaccounted-for position-mode: " + mode);

    if (hasReachedEnd())
      throw new IllegalStateException("No next char available");

    return new StringPosition(this, charIndex + 1);
  }

  public StringPosition getPosition() {
    return getPosition(PositionMode.CURRENT);
  }

  public boolean startsWith(@NotNull String value, boolean ignoreCase) {
    return _getFirstMismatchIndex(value, ignoreCase) < 0;
  }

  public boolean contentEquals(@NotNull StringView value, boolean ignoreCase) {
    // TODO: Building the other view is really not that optimal...
    return contentEquals(value.buildString(), ignoreCase);
  }

  public boolean contentEquals(@NotNull String value, boolean ignoreCase) {
    if (length() != value.length())
      return false;

    return _getFirstMismatchIndex(value, ignoreCase) < 0;
  }

  public int _getFirstMismatchIndex(@NotNull String value, boolean ignoreCase) {
    int valueIndex = 0;

    if (length() < value.length())
      return endExclusive.charIndex;

    for (int index = startInclusive.charIndex; index < endExclusive.charIndex; ++index) {
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

  public char lastChar() {
    int targetIndex = endExclusive.charIndex - 1;

    while (targetIndex >= startInclusive.charIndex && removeIndices.get(targetIndex))
      --targetIndex;

    if (targetIndex < startInclusive.charIndex)
      return 0;

    return contents.charAt(targetIndex);
  }

  public char nthChar(int index) {
    int targetIndex = startInclusive.charIndex + index;

    while (targetIndex < endExclusive.charIndex - 1 && removeIndices.get(targetIndex))
      ++targetIndex;

    if (targetIndex >= endExclusive.charIndex)
      return 0;

    return contents.charAt(targetIndex);
  }

  public double parseDouble() throws NumberFormatException {
    // TODO: I'm kinda itching to write my own parser here, which doesn't allocate the intermediate char[]
    return Double.parseDouble(buildString());
  }

  public long parseLong() throws NumberFormatException {
    // TODO: I'm kinda itching to write my own parser here, which doesn't allocate the intermediate char[]
    return Long.parseLong(buildString());
  }

  public boolean isEmpty() {
    return length() == 0;
  }

  public int length() {
    return endExclusive.charIndex - startInclusive.charIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StringView))
      return false;

    StringView that = (StringView) o;

    return contentEquals(that.contents, false);
  }

  @Override
  public int hashCode() {
    int hash = 7;

    for (int inputIndex = startInclusive.charIndex; inputIndex < endExclusive.charIndex; ++inputIndex) {
      if (!removeIndices.get(inputIndex)) {
        char currentChar = contents.charAt(inputIndex);

        if (lowercase)
          currentChar = Character.toLowerCase(currentChar);

        hash = hash * 31 + currentChar;
      }
    }

    return hash;
  }
}
