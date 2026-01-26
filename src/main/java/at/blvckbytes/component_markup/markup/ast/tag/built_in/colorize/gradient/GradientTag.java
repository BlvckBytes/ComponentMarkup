/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeAttributes;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeCharsNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeTag;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class GradientTag extends ColorizeTag {

  public GradientTag() {
    super(TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("gradient", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionList colors = attributes.getMandatoryExpressionList("color", "c");
    ExpressionList offsets = attributes.getOptionalExpressionList("offset", "o");
    ExpressionList zIndices = attributes.getOptionalExpressionList("z-index", "z");
    ColorizeAttributes baseAttributes = getBaseAttributes(attributes);

    return new ColorizeCharsNode(
      tagName,
      interpreter -> (
        new GradientNodeState(
          tagName,
          interpreter.getCurrentSubtreeDepth(),
          colors,
          offsets,
          zIndices,
          baseAttributes.getPhase(interpreter),
          baseAttributes.getFlags(interpreter),
          interpreter
        )
      ),
      tagName, children, letBindings
    );
  }
}
