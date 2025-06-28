package at.blvckbytes.component_markup.markup.ast.node.content;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TranslateNode extends ContentNode {

  public final ExpressionNode key;
  public final List<MarkupNode> with;
  public final @Nullable MarkupNode fallback;

  public TranslateNode(
    ExpressionNode key,
    List<MarkupNode> with,
    @Nullable MarkupNode fallback,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.key = key;
    this.with = with;
    this.fallback = fallback;
  }
}
