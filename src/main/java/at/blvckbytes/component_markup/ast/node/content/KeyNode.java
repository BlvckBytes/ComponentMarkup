package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class KeyNode extends ContentNode {

  public final String key;

  public KeyNode(String key, CursorPosition position, List<LetBinding> letBindings) {
    super(position, null, letBindings);

    this.key = key;
  }
}
