package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.parser.ParserChildItem;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class TerminalNode extends StyledNode implements ParserChildItem {

  public TerminalNode(CursorPosition position, @Nullable Set<LetBinding> letBindings) {
    super(position, null, letBindings);
  }
}
