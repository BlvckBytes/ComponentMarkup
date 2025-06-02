package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public abstract class InterpolationNode extends ContentNode {

  public InterpolationNode(
    CursorPosition position,
    List<LetBinding> letBindings
  ) {
    super(position, null, letBindings);
  }

  public abstract String getText();
}
