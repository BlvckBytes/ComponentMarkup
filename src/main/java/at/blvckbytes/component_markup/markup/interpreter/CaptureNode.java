package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;

import java.util.Collections;
import java.util.LinkedHashSet;

public class CaptureNode extends MarkupNode {

  public CaptureNode(MarkupNode node, LinkedHashSet<LetBinding> bindings) {
    super(node.position, Collections.singletonList(node), bindings);
  }
}
