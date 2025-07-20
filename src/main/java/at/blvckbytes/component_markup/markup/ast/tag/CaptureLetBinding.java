package at.blvckbytes.component_markup.markup.ast.tag;

import org.jetbrains.annotations.Nullable;

public class CaptureLetBinding extends LetBinding {

  public final @Nullable Object value;

  public CaptureLetBinding(@Nullable Object value, String name, LetBinding capture) {
    super(name, capture.position);

    this.value = value;
  }
}
