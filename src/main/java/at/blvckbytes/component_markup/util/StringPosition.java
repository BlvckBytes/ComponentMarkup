package at.blvckbytes.component_markup.util;

public class StringPosition {

  @JsonifyIgnore
  public final StringView rootView;

  public final int charIndex, lineNumber, columnNumber;

  public StringPosition(StringView rootView, int charIndex, int lineNumber, int columnNumber) {
    this.rootView = rootView;
    this.charIndex = charIndex;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }

  public StringPosition(StringView rootView, int charIndex) {
    this(rootView, charIndex, -1, -1);
  }
}
