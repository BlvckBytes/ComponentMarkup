package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContentNode extends AstNode {

  public final NodeStyle style;
  public final @Nullable List<AstNode> children;
  public final List<LetBinding> letBindings;

  public ContentNode(CursorPosition position, @Nullable List<AstNode> children, List<LetBinding> letBindings) {
    super(position);

    this.style = new NodeStyle();
    this.children = children;
    this.letBindings = letBindings;
  }
}
