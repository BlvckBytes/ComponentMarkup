package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AttributeMap {

  @NotNull ExpressionNode getMandatoryExpressionNode(String name);

  @Nullable ExpressionNode getOptionalExpressionNode(String name);

  @NotNull ExpressionList getMandatoryExpressionList(String name);

  @NotNull ExpressionList getOptionalExpressionList(String name);

  @NotNull MarkupNode getMandatoryMarkupNode(String name);

  @Nullable MarkupNode getOptionalMarkupNode(String name);

  @NotNull List<MarkupNode> getMandatoryMarkupList(String name);

  @NotNull List<MarkupNode> getOptionalMarkupList(String name);

}
