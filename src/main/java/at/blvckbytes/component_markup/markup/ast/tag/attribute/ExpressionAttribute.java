package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

import java.util.EnumSet;

public class ExpressionAttribute extends Attribute {

  public final ExpressionNode value;
  public final EnumSet<ExpressionFlag> flags;

  public ExpressionAttribute(
    CursorPosition position,
    String name,
    ExpressionNode value,
    ExpressionFlag... flags
  ) {
    super(position, name);

    this.value = value;
    this.flags = flags.length == 0 ? EnumSet.noneOf(ExpressionFlag.class) : EnumSet.of(flags[0], flags);
  }
}
