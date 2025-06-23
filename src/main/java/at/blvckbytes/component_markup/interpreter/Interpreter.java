package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Interpreter {

  String evaluateAsString(AExpression expression);

  long evaluateAsLong(AExpression expression);

  double evaluateAsDouble(AExpression expression);

  @Nullable Boolean evaluateAsBoolean(AExpression expression);

  List<Object> interpret(AstNode astNode);

  Object joinComponents(List<Object> components);

}
