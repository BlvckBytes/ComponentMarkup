package at.blvckbytes.component_markup.ast.node.control;

import at.blvckbytes.component_markup.ast.node.AstNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IfThenElseNode extends AstNode {

  public final List<ConditionalNode> conditions;
  public final @Nullable AstNode fallback;

  public IfThenElseNode(
    List<ConditionalNode> conditions,
    @Nullable AstNode fallback
  ) {
    super(conditions.get(0).position, null, null);

    this.conditions = conditions;
    this.fallback = fallback;
  }
}
