package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class StyledNode extends AstNode {

  private @Nullable NodeStyle style;

  public StyledNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
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

  public void setStyle(@Nullable NodeStyle style) {
    this.style = style;
  }
}
