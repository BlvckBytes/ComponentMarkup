package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TranslateNode extends ContentNode {

  public final ExpressionNode key;
  public final List<AstNode> with;
  public final @Nullable AstNode fallback;

  public TranslateNode(
    ExpressionNode key,
    List<AstNode> with,
    @Nullable AstNode fallback,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.key = key;
    this.with = with;
    this.fallback = fallback;
  }
}
