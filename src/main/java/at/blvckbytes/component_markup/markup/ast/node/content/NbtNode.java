package at.blvckbytes.component_markup.markup.ast.node.content;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt.NbtSource;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NbtNode extends ContentNode {

  public final NbtSource source;
  public final ExpressionNode identifier;
  public final ExpressionNode path;
  public final @Nullable ExpressionNode interpret;
  public final @Nullable MarkupNode separator;

  public NbtNode(
    NbtSource source,
    ExpressionNode identifier,
    ExpressionNode path,
    @Nullable ExpressionNode interpret,
    @Nullable MarkupNode separator,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.source = source;
    this.identifier = identifier;
    this.path = path;
    this.interpret = interpret;
    this.separator = separator;
  }
}
