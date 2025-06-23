package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import me.blvckbytes.gpeee.IExpressionEvaluator;

public interface InterpreterInterceptor {

  InterceptionResult interceptInterpretation(
    AstNode node,
    OutputBuilder builder,
    TemporaryMemberEnvironment environment,
    IExpressionEvaluator expressionEvaluator
  );

  void afterInterpretation(
    AstNode node,
    OutputBuilder builder,
    TemporaryMemberEnvironment environment,
    IExpressionEvaluator expressionEvaluator
  );
}
