package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;

import java.util.List;

public class TextNode extends ContentNode {

  public final String text;

  public TextNode(String text, List<AstNode> children, List<LetBinding> letBindings) {
    super(children, letBindings);

    this.text = text;
  }
}
