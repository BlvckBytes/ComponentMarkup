package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class StyledNode extends MarkupNode {

  private @Nullable NodeStyle style;

  public StyledNode(
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);
  }

  public @Nullable NodeStyle getStyle() {
    return this.style;
  }

  public @NotNull NodeStyle getOrInstantiateStyle() {
    if (this.style == null)
      this.style = new NodeStyle();

    return this.style;
  }
}
