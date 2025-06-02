package at.blvckbytes.component_markup.ast.node;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class IfThenElseNode extends AstNode {

  public final List<ConditionalNode> conditions;
  public final @Nullable ConditionalNode fallback;

  public IfThenElseNode(
    List<ConditionalNode> conditions,
    @Nullable ConditionalNode fallback
  ) {
    super(conditions.get(0).position, null, Collections.emptyList());

    this.conditions = conditions;
    this.fallback = fallback;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "IfThenElseNode{\n" +
      indent(indentLevel) + "conditions=" + stringifyList(conditions, indentLevel) + ",\n" +
      stringifySubtree(fallback, "fallback", indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
