package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;
import org.jetbrains.annotations.Nullable;

public class NbtParameter implements RendererParameter {

  public final NbtSource nbtSource;
  public final String identifier;
  public final String path;
  public final boolean interpret;
  public final @Nullable Object separator;

  public NbtParameter(NbtSource nbtSource, String identifier, String path, boolean interpret, @Nullable Object separator) {
    this.nbtSource = nbtSource;
    this.identifier = identifier;
    this.path = path;
    this.interpret = interpret;
    this.separator = separator;
  }

  @Override
  public String asPlainText() {
    return toString();
  }

  @Override
  public String toString() {
    return "NbtParameter{" +
      "nbtSource=" + nbtSource +
      ", identifier='" + identifier + '\'' +
      ", path='" + path + '\'' +
      ", interpret=" + interpret +
      ", separator=" + separator +
      '}';
  }
}
