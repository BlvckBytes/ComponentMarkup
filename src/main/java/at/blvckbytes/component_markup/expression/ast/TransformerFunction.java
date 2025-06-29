package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.ValueInterpreter;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TransformerFunction {

  @Nullable Object transform(@Nullable Object input, ValueInterpreter valueInterpreter);

}
