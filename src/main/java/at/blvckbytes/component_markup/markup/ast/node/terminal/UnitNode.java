package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class UnitNode extends TerminalNode {

  public UnitNode(StringView positionProvider, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(positionProvider, letBindings);
  }
}
