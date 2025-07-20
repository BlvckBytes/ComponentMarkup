package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class UnitNode extends TerminalNode {

  public UnitNode(CursorPosition position, @Nullable Set<LetBinding> letBindings) {
    super(position, letBindings);
  }
}
