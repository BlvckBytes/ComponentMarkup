package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ComponentSequence {

  public final @Nullable MarkupNode nonTerminal;
  public final List<Object> members;
  public final @Nullable ComputedStyle computedStyle;

  public ComponentSequence(@Nullable MarkupNode nonTerminal, Interpreter interpreter) {
    this.nonTerminal = nonTerminal;

    if (nonTerminal instanceof StyledNode)
      this.computedStyle = new ComputedStyle((StyledNode) nonTerminal, interpreter);
    else
      this.computedStyle = null;

    this.members = new ArrayList<>();
  }
}
