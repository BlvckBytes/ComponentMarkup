package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupList;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class TranslateNode extends UnitNode {

  public final ExpressionNode key;
  public final MarkupList with;
  public final @Nullable ExpressionNode fallback;

  public TranslateNode(
    ExpressionNode key,
    MarkupList with,
    @Nullable ExpressionNode fallback,
    CursorPosition position,
    @Nullable Set<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.key = key;
    this.with = with;
    this.fallback = fallback;
  }
}
