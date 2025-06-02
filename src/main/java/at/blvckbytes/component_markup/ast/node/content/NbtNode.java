package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.built_in.nbt.NbtSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NbtNode extends ContentNode {

  public final NbtSource source;
  public final String path;
  public final boolean interpret;
  public final @Nullable AstNode separator;

  public NbtNode(
    NbtSource source,
    String path,
    boolean interpret,
    @Nullable AstNode separator,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(children, letBindings);

    this.source = source;
    this.path = path;
    this.interpret = interpret;
    this.separator = separator;
  }
}
