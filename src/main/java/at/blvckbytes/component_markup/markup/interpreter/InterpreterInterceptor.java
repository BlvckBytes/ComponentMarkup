package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;

import java.util.EnumSet;

public interface InterpreterInterceptor {

  InterceptionResult interceptInterpretation(MarkupNode node, Interpreter interpreter);

  void afterInterpretation(MarkupNode node, Interpreter interpreter);

  void onSkippedByParent(MarkupNode node, Interpreter interpreter);

}
