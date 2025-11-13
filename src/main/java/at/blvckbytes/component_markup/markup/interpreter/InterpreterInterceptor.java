/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;

public interface InterpreterInterceptor {

  InterceptionResult interceptInterpretation(MarkupNode node, Interpreter<?, ?> interpreter);

  void afterInterpretation(MarkupNode node, Interpreter<?, ?> interpreter);

  void onSkippedByChild(MarkupNode node, Interpreter<?, ?> interpreter, InterceptionResult priorResult);

}
