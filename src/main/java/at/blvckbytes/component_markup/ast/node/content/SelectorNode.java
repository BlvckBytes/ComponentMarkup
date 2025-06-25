package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectorNode extends ContentNode {

  public final AExpression selector;
  public final @Nullable AstNode separator;

  public SelectorNode(
    AExpression selector,
    @Nullable AstNode separator,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.selector = selector;
    this.separator = separator;
  }
}
