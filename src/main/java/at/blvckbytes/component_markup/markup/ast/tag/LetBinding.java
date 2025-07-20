package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

import java.util.Objects;

public abstract class LetBinding {

  public final String name;
  public final CursorPosition position;

  public LetBinding(
    String name,
    CursorPosition position
  ) {
    this.name = name;
    this.position = position;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LetBinding)) return false;
    LetBinding that = (LetBinding) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }
}
