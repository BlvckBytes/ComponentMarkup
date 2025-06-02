package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectorNode extends ContentNode {

  public final String selector;
  public final @Nullable AstNode separator;

  public SelectorNode(
    String selector,
    @Nullable AstNode separator,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(children, letBindings);

    this.selector = selector;
    this.separator = separator;
  }
}
