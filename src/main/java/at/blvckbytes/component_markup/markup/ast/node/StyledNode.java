package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public abstract class StyledNode extends MarkupNode {

  private @Nullable NodeStyle style;

  public StyledNode(
    StringView positionProvider,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, children, letBindings);
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
