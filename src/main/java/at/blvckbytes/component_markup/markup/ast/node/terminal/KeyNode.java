package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringPosition;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public class KeyNode extends UnitNode {

  public final ExpressionNode key;

  public KeyNode(
    ExpressionNode key,
    StringPosition position,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.key = key;
  }
}
