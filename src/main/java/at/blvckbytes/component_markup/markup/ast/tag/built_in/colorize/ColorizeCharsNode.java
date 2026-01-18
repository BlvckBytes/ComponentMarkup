/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.node.terminal.UnitNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.OutputBuilder;
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
  protected boolean handleTextAndGetIfDoProcess(TextNode node, ColorizeNodeState state, Interpreter<?, ?> interpreter) {
    OutputBuilder<?, ?> builder = interpreter.getCurrentBuilder();
    NodeStyle nodeStyle = node.getStyle();

    boolean skipWhitespace = state.flags.contains(ColorizeFlag.SKIP_WHITESPACE);

    int lastIndex = node.textValue.length() - 1;

    for (int charIndex = 0; charIndex <= lastIndex; ++charIndex) {
      char currentChar = node.textValue.charAt(charIndex);

      String nodeContents;

      if (!skipWhitespace || charIndex == lastIndex)
        nodeContents = String.valueOf(currentChar);

      else {
        int beginIndex = charIndex;

        while (charIndex < lastIndex) {
          if (!Character.isWhitespace(node.textValue.charAt(charIndex + 1)))
            break;

          ++charIndex;
        }

        nodeContents = node.textValue.substring(beginIndex, charIndex + 1);
      }

      TextNode charNode = new TextNode(InputView.EMPTY, nodeContents);

      if (nodeStyle != null) {
        NodeStyle charStyle = charNode.getOrInstantiateStyle();
        charStyle.inheritFrom(nodeStyle);
        // Block out inherited colors, seeing how we're overwriting them later. Otherwise,
        // the rainbow-color may be overwritten at a later point in time when the component
        // is actually finalized, which happens way after char-colorization.
        charStyle.color = null;
      }

      // No need to buffer, as all chars will be colored differently
      builder.onText(charNode, skipWhitespace && Character.isWhitespace(currentChar) ? null : state::addInjected, true);
    }

    return false;
  }

  @Override
  protected boolean handleUnitAndGetIfDoProcess(UnitNode node, ColorizeNodeState state, Interpreter<?, ?> interpreter) {
    interpreter.getCurrentBuilder().onUnit(node, state::addInjected);
    return false;
  }
}
