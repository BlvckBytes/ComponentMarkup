package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;

import java.util.List;

public class AchievementTooltipNode extends ContentNode {

  public final String value;

  public AchievementTooltipNode(String value, List<AstNode> children, List<LetBinding> letBindings) {
    super(children, letBindings);

    this.value = value;
  }
}
