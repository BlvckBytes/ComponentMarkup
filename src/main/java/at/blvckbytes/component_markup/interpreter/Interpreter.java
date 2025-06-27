package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Interpreter {

  // TODO: Would also be cool if interceptors could specify temporary members using this interface

  @NotNull String evaluateAsString(ExpressionNode expression);

  @Nullable String evaluateAsStringOrNull(ExpressionNode expression);

  long evaluateAsLong(ExpressionNode expression);

  @Nullable Long evaluateAsLongOrNull(ExpressionNode expression);

  double evaluateAsDouble(ExpressionNode expression);

  @Nullable Double evaluateAsDoubleOrNull(ExpressionNode expression);

  boolean evaluateAsBoolean(ExpressionNode expression);

  @Nullable Boolean evaluateAsBooleanOrNull(ExpressionNode expression);

  List<Object> interpret(AstNode astNode, char breakChar);

  OutputBuilder getCurrentBuilder();

  ComponentConstructor getComponentConstructor();

  boolean isInSubtree();

}
