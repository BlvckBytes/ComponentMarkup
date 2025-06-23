package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Interpreter {

  // TODO: Add nullable versions

  String evaluateAsString(AExpression expression);

  long evaluateAsLong(AExpression expression);

  double evaluateAsDouble(AExpression expression);

  @Nullable Boolean evaluateAsBoolean(AExpression expression);

  List<Object> interpret(AstNode astNode, char breakChar);

  OutputBuilder getCurrentBuilder();

  ComponentConstructor getComponentConstructor();

  boolean isInSubtree();

}
