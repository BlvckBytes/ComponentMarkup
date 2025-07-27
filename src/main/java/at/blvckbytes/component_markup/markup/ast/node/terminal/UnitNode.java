package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public class UnitNode extends TerminalNode {

  public UnitNode(int position, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(position, letBindings);
  }
}
