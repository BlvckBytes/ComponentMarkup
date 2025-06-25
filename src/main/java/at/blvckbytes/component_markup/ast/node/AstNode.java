package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AstNode extends Jsonifiable {

  public final CursorPosition position;
  public final @Nullable List<AstNode> children;
  public final @Nullable List<LetBinding> letBindings;

  public AstNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    this.position = position;
    this.children = children;
    this.letBindings = letBindings;
  }
}
