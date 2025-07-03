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

  public static ComponentSequence initial(ComputedStyle defaultStyle) {
    return new ComponentSequence(null, null);
  }

  public static ComponentSequence next(MarkupNode nonTerminal, Interpreter interpreter, ComponentSequence parentSequence) {
    ComputedStyle computedStyle = null;

    if (nonTerminal instanceof StyledNode)
      computedStyle = new ComputedStyle((StyledNode) nonTerminal, interpreter);

    return new ComponentSequence(nonTerminal, computedStyle);
  }

  public ComponentSequence(@Nullable MarkupNode nonTerminal, @Nullable ComputedStyle computedStyle) {
    this.nonTerminal = nonTerminal;
    this.members = new ArrayList<>();
    this.computedStyle = computedStyle;
  }
}
