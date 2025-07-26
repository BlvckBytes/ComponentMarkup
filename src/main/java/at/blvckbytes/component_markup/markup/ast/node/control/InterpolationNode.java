package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.parser.ParserChildItem;
import at.blvckbytes.component_markup.util.StringPosition;

public class InterpolationNode extends StyledNode implements ParserChildItem {

  public final ExpressionNode contents;

  public InterpolationNode(ExpressionNode contents, StringPosition position) {
    super(position, null, null);

    this.contents = contents;
  }
}
