package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;

import java.util.List;

public class KeyNode extends ContentNode {

  public KeyNode(String key, List<LetBinding> letBindings) {
    super(null, letBindings);
  }
}
