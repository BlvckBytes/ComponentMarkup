package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;

import java.util.EnumSet;

public interface InterpreterInterceptor {

  EnumSet<InterceptionFlag> interceptInterpretation(AstNode node, OutputBuilder builder, Interpreter interpreter);

  void afterInterpretation(AstNode node, OutputBuilder builder, Interpreter interpreter);

  void onSkippedByOther(AstNode node, OutputBuilder builder, Interpreter interpreter);

}
