package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.parser.ParserChildItem;
import at.blvckbytes.component_markup.util.StringPosition;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class TerminalNode extends StyledNode implements ParserChildItem {

  public TerminalNode(StringPosition position, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(position, null, letBindings);
  }
}
