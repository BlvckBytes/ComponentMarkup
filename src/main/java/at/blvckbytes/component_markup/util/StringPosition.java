package at.blvckbytes.component_markup.util;

public class StringPosition {

  // TODO: It would be really nice if string-positions could just be integers, and the view-ref is kept where needed

  @JsonifyIgnore
  public final StringView rootView;

  public final int charIndex;

  public StringPosition(StringView rootView, int charIndex) {
    this.rootView = rootView;
    this.charIndex = charIndex;
  }

  public StringPosition prior() {
    if (charIndex <= 0)
      throw new IllegalStateException("There's no prior character");

    return new StringPosition(rootView, charIndex - 1);
  }
}
