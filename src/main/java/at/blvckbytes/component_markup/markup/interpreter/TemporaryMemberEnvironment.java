package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.interpreter.ValueInterpreter;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class TemporaryMemberEnvironment implements InterpretationEnvironment {

  private final InterpretationEnvironment baseEnvironment;
  private final Map<String, Stack<Object>> shadowingStaticVariables;

  public TemporaryMemberEnvironment(InterpretationEnvironment baseEnvironment) {
    this.baseEnvironment = baseEnvironment;
    this.shadowingStaticVariables = new HashMap<>();
  }

  public void pushVariable(String name, Object value) {
    shadowingStaticVariables.computeIfAbsent(name, k -> new Stack<>()).push(value);
  }

  public void updateVariable(String name, Object value) {
    Stack<Object> valueStack = shadowingStaticVariables.computeIfAbsent(name, k -> new Stack<>());

    if (valueStack.isEmpty()) {
      valueStack.push(value);
      return;
    }

    valueStack.set(valueStack.size() - 1, value);
  }

  public void popVariable(String name) {
    Stack<Object> valueStack = shadowingStaticVariables.computeIfAbsent(name, k -> new Stack<>());

    if (valueStack.isEmpty()) {
      LoggerProvider.get().log(Level.WARNING, "The temporary variable-stack for variable " + name + " was unbalanced");
      return;
    }

    valueStack.pop();
  }

  @Override
  public @Nullable Object getVariableValue(String name) {
    Stack<Object> valueStack = shadowingStaticVariables.get(name);

    if (valueStack != null && !valueStack.isEmpty())
      return valueStack.peek();

    return baseEnvironment.getVariableValue(name);
  }

  @Override
  public boolean doesVariableExist(String name) {
    Stack<Object> valueStack = shadowingStaticVariables.get(name);

    if (valueStack != null && !valueStack.isEmpty())
      return true;

    return baseEnvironment.doesVariableExist(name);
  }

  @Override
  public ValueInterpreter getValueInterpreter() {
    return baseEnvironment.getValueInterpreter();
  }
}
