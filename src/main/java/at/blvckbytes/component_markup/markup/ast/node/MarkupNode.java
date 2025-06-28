package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MarkupNode extends Jsonifiable {

  public final CursorPosition position;
  public final @Nullable List<MarkupNode> children;
  public final @Nullable List<LetBinding> letBindings;

  public MarkupNode(
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    this.position = position;
    this.children = children;
    this.letBindings = letBindings;
  }
}
