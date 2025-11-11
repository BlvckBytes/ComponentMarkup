/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.interpreter.ValueInterpreter;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class TemporaryMemberEnvironment extends InterpretationEnvironment {

  private static final Object NULL_SENTINEL = new Object();

  private final InterpretationEnvironment baseEnvironment;
  private final Stack<Map<String, Object>> scopeStack;

  public TemporaryMemberEnvironment(InterpretationEnvironment baseEnvironment) {
    super(new HashMap<>(), baseEnvironment.getValueInterpreter(), baseEnvironment.interpretationPlatform, baseEnvironment.context);

    this.baseEnvironment = baseEnvironment;
    this.scopeStack = new Stack<>();
  }

  public void beginScope() {
    scopeStack.push(new HashMap<>());
  }

  public void endScope() {
    if (scopeStack.empty()) {
      LoggerProvider.log(Level.WARNING, "Tried to end a scope on an empty scope-stack");
      return;
    }

    scopeStack.pop();
  }

  public void setScopeVariable(String name, Object value) {
    if (scopeStack.empty()) {
      LoggerProvider.log(Level.WARNING, "Tried to set a scope-variable outside of having begun a scope");
      return;
    }

    scopeStack.peek().put(name, value);
  }

  public void forEachKnownName(Consumer<String> handler) {
    Set<String> encounteredNames = new HashSet<>();

    for (int scopeIndex = scopeStack.size() - 1; scopeIndex >= 0; --scopeIndex) {
      for (String scopeVariableName : scopeStack.get(scopeIndex).keySet()) {
        if (encounteredNames.add(scopeVariableName))
          handler.accept(scopeVariableName);
      }
    }

    for (String baseName : baseEnvironment.getNames()) {
      if (encounteredNames.add(baseName))
        handler.accept(baseName);
    }
  }

  @Override
  public @Nullable Object getVariableValue(String name) {
    for (int scopeIndex = scopeStack.size() - 1; scopeIndex >= 0; --scopeIndex) {
      Map<String, Object> scopeVariables = scopeStack.get(scopeIndex);
      Object value = scopeVariables.getOrDefault(name, NULL_SENTINEL);

      if (value == NULL_SENTINEL)
        continue;

      return value;
    }

    return baseEnvironment.getVariableValue(name);
  }

  @Override
  public boolean doesVariableExist(String name) {
    for (int scopeIndex = scopeStack.size() - 1; scopeIndex >= 0; --scopeIndex) {
      Map<String, Object> scopeVariables = scopeStack.get(scopeIndex);

      if (scopeVariables.containsKey(name))
        return true;
    }

    return baseEnvironment.doesVariableExist(name);
  }

  @Override
  public ValueInterpreter getValueInterpreter() {
    return baseEnvironment.getValueInterpreter();
  }
}
