package at.blvckbytes.component_markup.ast.node;

import java.util.List;

public class IfThenElseNode extends AstNode {

  public final List<ConditionalNode> conditions;
  public final ConditionalNode fallback;

  public IfThenElseNode(
    List<ConditionalNode> conditions,
    ConditionalNode fallback
  ) {
    super(conditions.get(0).position);

    this.conditions = conditions;
    this.fallback = fallback;
  }
}
