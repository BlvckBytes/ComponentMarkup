package at.blvckbytes.component_markup.util;

public class StringPosition {

  @JsonifyIgnore
  public final StringView rootView;

  public final int charIndex;

  public StringPosition(StringView rootView, int charIndex) {
    this.rootView = rootView;
    this.charIndex = charIndex;
  }
}
