/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.UnitNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

public class ColorizeCharsNode extends ColorizeNode {

  public ColorizeCharsNode(
    InputView tagName,
    Function<Interpreter<?, ?>, ColorizeNodeState> stateCreator,
    InputView positionProvider,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(tagName, stateCreator, positionProvider, children, letBindings);
  }

  @Override
  protected void onTextEncounter(TextNode node, ColorizeNodeState state, Interpreter<?, ?> interpreter) {
    interpreter.getCurrentBuilder().onText(node, extendedBuilder -> state.addCandidate(extendedBuilder, node), true);
  }

  @Override
  protected void onUnitEncounter(UnitNode node, ColorizeNodeState state, Interpreter<?, ?> interpreter) {
    interpreter.getCurrentBuilder().onUnit(node, extendedBuilder -> state.addCandidate(extendedBuilder, node));
  }
}
