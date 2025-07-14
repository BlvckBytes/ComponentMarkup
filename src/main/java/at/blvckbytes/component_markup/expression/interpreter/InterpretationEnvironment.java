package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InterpretationEnvironment {

  private static final DefaultValueInterpreter DEFAULT_INTERPRETER = new DefaultValueInterpreter();

  protected final Map<String, Object> variables;
  protected ValueInterpreter valueInterpreter;

  public InterpretationEnvironment() {
    this(new HashMap<>(), DEFAULT_INTERPRETER);
  }

  private InterpretationEnvironment(Map<String, Object> variables, ValueInterpreter valueInterpreter) {
    this.variables = variables;
    this.valueInterpreter = valueInterpreter;
  }

  public @Nullable Object getVariableValue(String name) {
    return variables.get(name);
  }

  public boolean doesVariableExist(String name) {
    return variables.containsKey(name);
  }

  public ValueInterpreter getValueInterpreter() {
    return valueInterpreter;
  }

  public InterpretationEnvironment withVariable(String name, Object value) {
    this.variables.put(name, value);
    return this;
  }

  public InterpretationEnvironment removeVariable(String name) {
    this.variables.remove(name);
    return this;
  }

  public InterpretationEnvironment withValueInterpreter(ValueInterpreter valueInterpreter) {
    this.valueInterpreter = valueInterpreter;
    return this;
  }

  public InterpretationEnvironment copy() {
    return new InterpretationEnvironment(new HashMap<>(variables), valueInterpreter);
  }
}
