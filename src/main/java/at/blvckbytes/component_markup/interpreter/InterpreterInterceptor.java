package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;

public interface InterpreterInterceptor {

  InterceptionResult interceptInterpretation(AstNode node, OutputBuilder builder, Interpreter interpreter);

  void afterInterpretation(AstNode node, OutputBuilder builder, Interpreter interpreter);
}
