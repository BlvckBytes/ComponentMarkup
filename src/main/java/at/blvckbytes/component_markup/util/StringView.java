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

  public final StringPosition viewStart;
  public final StringPosition viewEnd;

  private int charIndex;
  private int lineNumber;
  private int columnNumber;

  @JsonifyIgnore
  private char priorChar;

  @JsonifyIgnore
  private @Nullable StringPosition subViewStart;

  private StringView(
    String contents,
    @Nullable StringView rootView,
    BitFlagArray removeIndices,
    boolean lowercase,
    @Nullable StringPosition viewStart,
    @Nullable StringPosition viewEnd
  ) {
    this.contents = contents;
    this.removeIndices = removeIndices;
    this.lowercase = lowercase;

    if (rootView == null)
      rootView = this;

    this.rootView = rootView;

    if (viewStart == null)
      viewStart = new StringPosition(rootView, 0, 0, 0);

    if (viewEnd == null)
      viewEnd = new StringPosition(rootView, contents.length() - 1, 0, 0);

    this.viewStart = viewStart;
    this.viewEnd = viewEnd;

    this.charIndex = viewStart.charIndex;

    if (charIndex == 0)
      --charIndex;

    this.lineNumber = viewStart.lineNumber;
    this.columnNumber = viewStart.columnNumber;
    this.priorChar = 0;
  }

  public void setLowercase() {
    lowercase = true;
  }

  public static StringView of(String contents) {
    int contentsLength = contents.length();

    if (contentsLength == 0)
      throw new IllegalStateException("Cannot instantiate an empty view");

    return new StringView(contents, null, new BitFlagArray(contentsLength), false, null, null);
  }

  public StringView buildSubViewUntilEnd(int start) {
    return buildSubViewAbsolute(start, viewEnd.charIndex);
  }

  public StringView buildSubViewRelative(int start) {
    if (start < 0)
      throw new IllegalStateException("Start " + start + " cannot be negative when in relative-mode");

    return buildSubViewAbsolute(viewStart.charIndex + start, viewEnd.charIndex);
  }

  public StringView buildSubViewRelative(int start, int end) {
    if (start < 0)
      throw new IllegalStateException("Start " + start + " cannot be negative when in relative-mode");

    int endIndex = end + (end < 0 ? viewEnd.charIndex : viewStart.charIndex);

    return buildSubViewAbsolute(viewStart.charIndex + start, endIndex);
  }

  public StringView buildSubViewAbsolute(int start, int end) {
    if (start < viewStart.charIndex || start > viewEnd.charIndex)
      throw new IllegalStateException("Start " + start + " out of this view's range: [" + viewStart.charIndex + ";" + viewEnd.charIndex + "]");

    if (end < viewStart.charIndex || end > viewEnd.charIndex)
      throw new IllegalStateException("End " + end + " out of this view's range: [" + viewStart.charIndex + ";" + viewEnd.charIndex + "]");

    if (end < start)
      throw new IllegalStateException("End " + end + " lies before start " + start);

    if (start == viewStart.charIndex && end == viewEnd.charIndex)
      return this;

    return new StringView(contents, rootView, removeIndices, lowercase, new StringPosition(this, start), new StringPosition(this, end));
  }

  public StringView buildSubViewUntilPosition(PositionMode mode) {
    return buildSubViewUntilPosition(getPosition(mode));
  }

  public StringView buildSubViewUntilPosition(StringPosition position) {
    if (subViewStart == null)
      throw new IllegalStateException("Cannot build a sub-StringView without a determined start");

    StringView subView = new StringView(contents, rootView, removeIndices, lowercase, subViewStart, position);

    subViewStart = null;

    return subView;
  }

  public int getCharIndex() {
    return Math.max(this.charIndex, 0);
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void addIndexToBeRemoved(int index) {
    if (index < viewStart.charIndex || index > viewEnd.charIndex)
      throw new IllegalStateException("Index " + index + " out of this view's range: [" + viewStart.charIndex + ";" + viewEnd.charIndex + "]");

    removeIndices.set(index);
  }

  public char priorChar() {
    return priorChar;
  }

  public char nextChar() {
    if (charIndex >= viewEnd.charIndex)
      return 0;

    priorChar = contents.charAt(++charIndex);

    if (priorChar == '\n') {
      ++lineNumber;
      columnNumber = 0;
      return priorChar;
    }

    ++columnNumber;

    return priorChar;
  }

  public char peekChar() {
    if (charIndex >= viewEnd.charIndex)
      return 0;

    if (charIndex < 0)
      charIndex = 0;

    return contents.charAt(charIndex);
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

    if (position.charIndex < viewStart.charIndex || position.charIndex > viewEnd.charIndex)
      throw new IllegalStateException("Index " + position.charIndex + " out of this view's range: [" + viewStart.charIndex + ";" + viewEnd.charIndex + "]");

    subViewStart = position;
  }

  public @Nullable StringPosition getSubViewStart() {
    return subViewStart;
  }

  public String buildString() {
    return buildString(SubstringFlag.NONE);
  }

  public String buildString(EnumSet<SubstringFlag> flags) {
    int maxSubstringLength = viewEnd.charIndex - viewStart.charIndex + 1;

    char[] result = new char[maxSubstringLength];
    int nextResultIndex = 0;

    boolean doIgnoreWhitespace = false;

    for (int inputIndex = viewStart.charIndex; inputIndex <= viewEnd.charIndex; ++inputIndex) {
      if (removeIndices.get(inputIndex))
        continue;

      char currentChar = contents.charAt(inputIndex);

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

      if (lowercase)
        currentChar = Character.toLowerCase(currentChar);

      result[nextResultIndex++] = currentChar;
    }

    if (flags.contains(SubstringFlag.REMOVE_TRAILING_SPACE)) {
      while (nextResultIndex > 0 && result[nextResultIndex - 1] == ' ')
        --nextResultIndex;
    }

    return new String(result, 0, nextResultIndex);
  }

  public void consumeWhitespace(@Nullable TokenOutput tokenOutput) {
    while (Character.isWhitespace(peekChar())) {
      nextChar();

      if (tokenOutput != null)
        tokenOutput.emitCharToken(getPosition(), TokenType.ANY__WHITESPACE);
    }
  }

  public void restorePosition(StringPosition position) {
    if (position.charIndex < viewStart.charIndex || position.charIndex > viewEnd.charIndex)
      throw new IllegalStateException("Position at " + position.charIndex + " out of this view's range: [" + viewStart.charIndex + ";" + viewEnd.charIndex + "]");

    if (position.charIndex != viewEnd.charIndex)
      removeIndices.clearRange(position.charIndex + 1, viewEnd.charIndex);

    charIndex = position.charIndex;
    lineNumber = position.lineNumber;
    columnNumber = position.columnNumber;
  }

  public StringPosition getPosition(PositionMode mode) {
    if (mode == PositionMode.CURRENT)
      return new StringPosition(this, getCharIndex(), lineNumber, columnNumber);

    if (mode == PositionMode.PRIOR) {
      // The last consumed character wasn't a newline, or we're already at the first line
      if (columnNumber > 0 || lineNumber < 2)
        return new StringPosition(this, getCharIndex() - 1, lineNumber, columnNumber - 1);

      return new StringPosition(this, getCharIndex() - 1, lineNumber - 1, columnNumber - 1);
    }

    if (mode != PositionMode.NEXT)
      throw new IllegalStateException("Unaccounted-for position-mode: " + mode);

    int newLineNumber = lineNumber;
    int newColumnNumber = columnNumber;

    if (peekChar() == '\n') {
      ++newLineNumber;
      newColumnNumber = 1;
    }

    return new StringPosition(this, getCharIndex() + 1, newLineNumber, newColumnNumber);
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
    if (remainingLength() != value.length())
      return false;

    return _getFirstMismatchIndex(value, ignoreCase) < 0;
  }

  private int remainingLength() {
    int value = length();

    if (charIndex >= 0)
      value -= charIndex + 1;

    return value;
  }

  public int _getFirstMismatchIndex(@NotNull String value, boolean ignoreCase) {
    int valueIndex = 0;

    if (remainingLength() < value.length())
      return viewEnd.charIndex;

    for (int index = getCharIndex(); index < viewEnd.charIndex; ++index) {
      if (removeIndices.get(index))
        continue;

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
    int targetIndex = viewEnd.charIndex;

    while (targetIndex >= viewStart.charIndex && removeIndices.get(targetIndex))
      --targetIndex;

    if (targetIndex < viewStart.charIndex)
      return 0;

    return contents.charAt(targetIndex);
  }

  public char nthChar(int index) {
    int targetIndex = viewStart.charIndex + index;

    while (targetIndex <= viewEnd.charIndex && removeIndices.get(targetIndex))
      ++targetIndex;

    if (targetIndex > viewEnd.charIndex)
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
    return viewEnd.charIndex - viewStart.charIndex + 1;
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

    for (int inputIndex = viewStart.charIndex; inputIndex <= viewEnd.charIndex; ++inputIndex) {
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
