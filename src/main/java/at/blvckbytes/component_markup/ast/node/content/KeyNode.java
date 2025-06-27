package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KeyNode extends ContentNode {

  public final ExpressionNode key;

  public KeyNode(
    ExpressionNode key,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.key = key;
  }
}
