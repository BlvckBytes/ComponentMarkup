package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityTooltipNode extends ContentNode {

  public final String type;
  public final String id;
  public final @Nullable AstNode name;

  public EntityTooltipNode(
    String type,
    String id,
    @Nullable AstNode name,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.type = type;
    this.id = id;
    this.name = name;
  }
}
