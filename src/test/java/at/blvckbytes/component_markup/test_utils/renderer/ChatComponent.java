package at.blvckbytes.component_markup.test_utils.renderer;

import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ChatComponent {

  public final String text;
  public final List<ChatComponent> extra;

  private final EnumSet<Format> formats;
  private final @Nullable Color color;
  private final @Nullable ChatComponent parent;

  public @Nullable Dimension totalDimension;
  public @Nullable Dimension selfDimension;

  public ChatComponent(String text, @Nullable Color color, @Nullable ChatComponent parent) {
    this.text = text;
    this.extra = new ArrayList<>();

    this.formats = EnumSet.noneOf(Format.class);
    this.color = color;
    this.parent = parent;
  }

  public void enableFormat(Format format) {
    this.formats.add(format);
  }

  public boolean hasFormat(Format format) {
    if (this.formats.contains(format))
      return true;

    if (this.parent != null)
      return parent.hasFormat(format);

    return false;
  }

  public @Nullable Color getColor() {
    if (this.color != null)
      return this.color;

    if (this.parent != null)
      return this.parent.getColor();

    return null;
  }
}
