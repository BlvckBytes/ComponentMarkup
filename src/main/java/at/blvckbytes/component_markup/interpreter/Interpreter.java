package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;

public interface Interpreter {

  String evaluateAsString(AExpression expression, IEvaluationEnvironment environment);

  long evaluateAsLong(AExpression expression, IEvaluationEnvironment environment);

  double evaluateAsDouble(AExpression expression, IEvaluationEnvironment environment);

  boolean evaluateAsBoolean(AExpression expression, IEvaluationEnvironment environment);

  List<Object> interpret(AstNode astNode, IEvaluationEnvironment environment);

  Object joinComponents(List<Object> components, IEvaluationEnvironment environment);

}
