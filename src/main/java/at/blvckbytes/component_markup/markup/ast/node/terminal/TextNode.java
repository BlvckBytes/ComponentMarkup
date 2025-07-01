package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextNode extends TerminalNode {

  public final ExpressionNode text;

  public TextNode(
    ExpressionNode text,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.text = text;
  }
}
