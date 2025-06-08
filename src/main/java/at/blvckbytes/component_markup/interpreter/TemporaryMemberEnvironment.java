package at.blvckbytes.component_markup.interpreter;

import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.gpeee.interpreter.IValueInterpreter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TemporaryMemberEnvironment implements IEvaluationEnvironment {

  private final IEvaluationEnvironment baseEnvironment;
  private final Map<String, Stack<Object>> shadowingStaticVariables;

  public TemporaryMemberEnvironment(IEvaluationEnvironment baseEnvironment) {
    this.baseEnvironment = baseEnvironment;
    this.shadowingStaticVariables = new HashMap<>();
  }

  public void pushVariables(Map<String, Object> variables) {
    for (Map.Entry<String, Object> entry : variables.entrySet())
      pushVariable(entry.getKey(), entry.getValue());
  }

  public void popVariables(Set<String> names) {
    for (String name : names)
      popVariable(name);
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
    shadowingStaticVariables.computeIfAbsent(name, k -> new Stack<>()).pop();
  }

  @Override
  public @Nullable AExpressionFunction getFunction(String name) {
    return baseEnvironment.getFunction(name);
  }

  @Override
  public @Nullable Object getVariable(String name) {
    Stack<Object> valueStack = shadowingStaticVariables.get(name);

    if (valueStack != null && !valueStack.isEmpty())
      return valueStack.peek();

    return baseEnvironment.getVariable(name);
  }

  @Override
  public boolean hasVariable(String name) {
    Stack<Object> valueStack = shadowingStaticVariables.get(name);

    if (valueStack != null && !valueStack.isEmpty())
      return true;

    return baseEnvironment.hasVariable(name);
  }

  @Override
  public IValueInterpreter getValueInterpreter() {
    return baseEnvironment.getValueInterpreter();
  }
}
