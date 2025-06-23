package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Interpreter {

  @NotNull String evaluateAsString(AExpression expression);

  @Nullable String evaluateAsStringOrNull(AExpression expression);

  long evaluateAsLong(AExpression expression);

  @Nullable Long evaluateAsLongOrNull(AExpression expression);

  double evaluateAsDouble(AExpression expression);

  @Nullable Double evaluateAsDoubleOrNull(AExpression expression);

  boolean evaluateAsBoolean(AExpression expression);

  @Nullable Boolean evaluateAsBooleanOrNull(AExpression expression);

  List<Object> interpret(AstNode astNode, char breakChar);

  OutputBuilder getCurrentBuilder();

  ComponentConstructor getComponentConstructor();

  boolean isInSubtree();

}
