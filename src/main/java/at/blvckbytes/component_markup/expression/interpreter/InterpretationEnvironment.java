package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;

public interface InterpretationEnvironment {

  @Nullable Object getVariableValue(String name);

  boolean doesVariableExist(String name);

  ValueInterpreter getValueInterpreter();

}
