/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.CaptureLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.InputView;

import java.util.Collections;
import java.util.LinkedHashSet;

public class CaptureNode extends MarkupNode {

  public CaptureNode(MarkupNode node, LinkedHashSet<LetBinding> bindings) {
    super(node.positionProvider, Collections.singletonList(node), bindings);
  }

  public static MarkupNode createVariableCapture(
    MarkupNode node,
    InterpretationEnvironment environment
  ) {
    LinkedHashSet<LetBinding> capturedBindings = new LinkedHashSet<>();

    environment.forEachKnownName(name -> {
      Object variableValue = environment.getVariableValue(name);

      if (variableValue instanceof InternalCopyable)
        variableValue = ((InternalCopyable) variableValue).copy();

      capturedBindings.add(new CaptureLetBinding(InputView.of(name), variableValue));
    });

    return new CaptureNode(node, capturedBindings);
  }
}
