package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public class UnitNode extends TerminalNode {

  public UnitNode(CursorPosition position, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(position, letBindings);
  }
}
