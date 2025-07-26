package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.StringPosition;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public class UnitNode extends TerminalNode {

  public UnitNode(StringPosition position, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(position, letBindings);
  }
}
