/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InterpretationEnvironment {

  public static final DefaultValueInterpreter DEFAULT_INTERPRETER = new DefaultValueInterpreter();

  protected final Map<String, Object> variables;
  protected ValueInterpreter valueInterpreter;

  public final InterpretationPlatform interpretationPlatform;

  public InterpretationEnvironment() {
    this(new HashMap<>(), DEFAULT_INTERPRETER, JavaInterpretationPlatform.INSTANCE);
  }

  public InterpretationEnvironment(Map<String, Object> variables, ValueInterpreter valueInterpreter, InterpretationPlatform interpretationPlatform) {
    this.variables = variables;
    this.valueInterpreter = valueInterpreter;
    this.interpretationPlatform = interpretationPlatform;
  }

  public Set<String> getNames() {
    return variables.keySet();
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
    return new InterpretationEnvironment(new HashMap<>(variables), valueInterpreter, interpretationPlatform);
  }
}
