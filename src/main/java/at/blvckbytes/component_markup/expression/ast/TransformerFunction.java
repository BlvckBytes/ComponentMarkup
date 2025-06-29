package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TransformerFunction {

  /**
   * @param input The resulting value of interpreting the wrapped node
   * @param environment Current environment used to interpret
   * @param interpreter Reference to the interpreter
   * @return Result of transforming the input-value
   */
  @Nullable Object transform(@Nullable Object input, InterpretationEnvironment environment, ExpressionInterpreter interpreter);

}
