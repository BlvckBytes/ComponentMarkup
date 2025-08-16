/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.test_utils;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.interpreter.JavaInterpretationPlatform;

import java.util.HashMap;

public class Environment extends InterpretationEnvironment {

  public Environment() {
    super(new HashMap<>(), InterpretationEnvironment.DEFAULT_INTERPRETER, JavaInterpretationPlatform.INSTANCE);
  }
}
