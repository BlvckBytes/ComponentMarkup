package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EnvironmentBuilder implements InterpretationEnvironment {

  private static final Object NULL_SENTINEL = new Object();

  private final Map<String, Object> staticVariables = new HashMap<>();
  private final Map<String, Supplier<@Nullable Object>> dynamicVariables = new HashMap<>();

  private @Nullable ValueInterpreter valueInterpreter;

  @Override
  public @Nullable Object getVariableValue(String name) {
    Object staticVariable;

    if ((staticVariable = staticVariables.getOrDefault(name, NULL_SENTINEL)) != NULL_SENTINEL)
      return staticVariable;

    Supplier<Object> dynamicVariable = dynamicVariables.get(name);

    if (dynamicVariable == null)
      return null;

    return dynamicVariable.get();
  }

  @Override
  public boolean doesVariableExist(String name) {
    return staticVariables.containsKey(name) || dynamicVariables.containsKey(name);
  }

  @Override
  public ValueInterpreter getValueInterpreter() {
    if (valueInterpreter == null)
      valueInterpreter = new DefaultValueInterpreter();

    return valueInterpreter;
  }

  public EnvironmentBuilder withValueInterpreter(ValueInterpreter valueInterpreter) {
    this.valueInterpreter = valueInterpreter;
    return this;
  }

  public EnvironmentBuilder withStatic(String name, @Nullable Object value) {
    dynamicVariables.remove(name);
    staticVariables.put(name, value);
    return this;
  }

  public EnvironmentBuilder withDynamic(String name, Supplier<@Nullable Object> value) {
    staticVariables.remove(name);
    dynamicVariables.put(name, value);
    return this;
  }
}
