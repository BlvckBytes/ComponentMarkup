package at.blvckbytes.component_markup.util;

public class StringPosition {

  @JsonifyIgnore
  public final int charIndex;

  public StringPosition(int charIndex) {
    this.charIndex = charIndex;
  }

  public StringPosition prior() {
    if (charIndex <= 0)
      throw new IllegalStateException("There's no prior character");

    return new StringPosition(charIndex - 1);
  }
}
