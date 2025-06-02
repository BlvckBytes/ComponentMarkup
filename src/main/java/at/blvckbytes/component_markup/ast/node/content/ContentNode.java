package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public abstract class ContentNode extends AstNode {

  public final NodeStyle style;
  public final @Nullable List<AstNode> children;
  public final List<LetBinding> letBindings;

  public ContentNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position);

    this.style = new NodeStyle();
    this.children = children;
    this.letBindings = letBindings;
  }

  // Convenience-method used when specifying ASTs in test-cases
  public ContentNode getStyle(Consumer<NodeStyle> handler) {
    handler.accept(this.style);
    return this;
  }
}
