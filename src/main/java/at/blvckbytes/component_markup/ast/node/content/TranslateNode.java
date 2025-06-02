package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class TranslateNode extends ContentNode {

  public final String key;
  public final List<AstNode> with;
  public final AstNode fallback;

  public TranslateNode(
    String key,
    List<AstNode> with,
    AstNode fallback,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.key = key;
    this.with = with;
    this.fallback = fallback;
  }
}
