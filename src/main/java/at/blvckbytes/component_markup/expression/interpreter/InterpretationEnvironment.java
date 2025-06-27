package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;

public interface InterpretationEnvironment {

  InterpretationEnvironment EMPTY_ENVIRONMENT = new InterpretationEnvironment() {

    @Override
    public @Nullable Object getVariableValue(String name) {
      return null;
    }

    @Override
    public boolean doesVariableExist(String name) {
      return false;
    }

    @Override
    public ValueInterpreter getValueInterpreter() {
      return new DefaultValueInterpreter();
    }
  };

  @Nullable Object getVariableValue(String name);

  boolean doesVariableExist(String name);

  ValueInterpreter getValueInterpreter();

}
