package at.blvckbytes.component_markup.markup.xml;

public enum StringBuilderMode {
  TEXT_MODE(true, false),
  TEXT_MODE_TRIM_TRAILING_SPACES(true, true),
  NORMAL_MODE(false, false)
  ;

  public final boolean textMode;
  public final boolean trimTrailingSpaces;

  StringBuilderMode(boolean textMode, boolean trimTrailingSpaces) {
    this.textMode = textMode;
    this.trimTrailingSpaces = trimTrailingSpaces;
  }
}
