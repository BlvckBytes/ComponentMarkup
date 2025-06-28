package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;

import java.util.EnumSet;

public interface InterpreterInterceptor {

  EnumSet<InterceptionFlag> interceptInterpretation(MarkupNode node, Interpreter interpreter);

  void afterInterpretation(MarkupNode node, Interpreter interpreter);

  void onSkippedByOther(MarkupNode node, Interpreter interpreter);

}
