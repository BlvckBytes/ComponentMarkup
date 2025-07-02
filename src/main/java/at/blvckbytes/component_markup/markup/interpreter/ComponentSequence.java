package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ComponentSequence {

  public final @Nullable MarkupNode nonTerminal;
  public final List<Object> members;

  public ComponentSequence(@Nullable MarkupNode nonTerminal) {
    this.nonTerminal = nonTerminal;
    this.members = new ArrayList<>();
  }
}
