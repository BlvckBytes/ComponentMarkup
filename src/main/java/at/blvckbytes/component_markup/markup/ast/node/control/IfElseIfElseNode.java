package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IfElseIfElseNode extends MarkupNode {

  public final List<MarkupNode> conditions;
  public final @Nullable MarkupNode fallback;

  public IfElseIfElseNode(
    List<MarkupNode> conditions,
    @Nullable MarkupNode fallback
  ) {
    super(conditions.get(0).positionProvider, null, null);

    this.conditions = conditions;
    this.fallback = fallback;
  }
}
