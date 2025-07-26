package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.util.StringView;

import java.util.Objects;

public abstract class LetBinding {

  public final StringView name;
  public final String plainName;

  public LetBinding(StringView name) {
    this.name = name;
    this.plainName = name.buildString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LetBinding)) return false;
    LetBinding that = (LetBinding) o;
    return Objects.equals(plainName, that.plainName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(plainName);
  }
}
