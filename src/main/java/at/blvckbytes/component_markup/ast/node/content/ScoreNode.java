package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreNode extends ContentNode {

  public final String name;
  public final String object;
  public final @Nullable String value;

  public ScoreNode(String name, String object, @Nullable String value, List<AstNode> children, List<LetBinding> letBindings) {
    super(children, letBindings);

    this.name = name;
    this.object = object;
    this.value = value;
  }
}
