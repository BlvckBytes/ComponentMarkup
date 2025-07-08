package at.blvckbytes.component_markup.util.json;

import org.jetbrains.annotations.Nullable;

public class _JsonString implements _JsonElement {

  public static _JsonString NULL = new _JsonString(null);

  public final @Nullable String value;

  public _JsonString(@Nullable String value) {
    this.value = value;
  }
}
