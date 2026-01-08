/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InterpretationEnvironment {

  public static final DefaultValueInterpreter DEFAULT_INTERPRETER = new DefaultValueInterpreter();

  protected final Map<String, Object> variables;
  protected ValueInterpreter valueInterpreter;

  public final InterpretationPlatform interpretationPlatform;
  public final Object context;

  public InterpretationEnvironment() {
    this(null);
  }

  public InterpretationEnvironment(Object context) {
    this(new HashMap<>(), DEFAULT_INTERPRETER, JavaInterpretationPlatform.INSTANCE, context);
  }

  public InterpretationEnvironment(
    Map<String, Object> variables,
    ValueInterpreter valueInterpreter,
    InterpretationPlatform interpretationPlatform,
    Object context
  ) {
    this.variables = variables;
    this.valueInterpreter = valueInterpreter;
    this.interpretationPlatform = interpretationPlatform;
    this.context = context;
  }

  public @Nullable Object getVariableValue(String name) {
    return variables.get(name);
  }

  public boolean doesVariableExist(String name) {
    return variables.containsKey(name);
  }

  public void forEachKnownName(Consumer<String> handler) {
    variables.keySet().forEach(handler);
  }

  public ValueInterpreter getValueInterpreter() {
    return valueInterpreter;
  }

  public InterpretationEnvironment withVariable(String name, Object value) {
    this.variables.put(name, value);
    return this;
  }

  public InterpretationEnvironment withValueInterpreter(ValueInterpreter valueInterpreter) {
    this.valueInterpreter = valueInterpreter;
    return this;
  }

  public InterpretationEnvironment copy() {
    return new InterpretationEnvironment(new HashMap<>(variables), valueInterpreter, interpretationPlatform, context);
  }

  public InterpretationEnvironment inheritFrom(InterpretationEnvironment other, boolean allowShadowing) {
    other.forEachKnownName(otherName -> {
      if (doesVariableExist(otherName) && !allowShadowing)
        return;

      this.variables.put(otherName, other.getVariableValue(otherName));
    });

    return this;
  }
}
