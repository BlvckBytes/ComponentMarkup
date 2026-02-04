/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.click;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public abstract class ClickTag extends TagDefinition {

  private final ClickAction action;
  private final String tagName;

  protected ClickTag(ClickAction action, String tagName) {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);

    this.tagName = tagName;
    this.action = action;
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals(this.tagName, true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode flagValue = attributes.getOptionalBoundFlagExpressionNode();

    return new ClickNode(
      action,
      flagValue == null ? attributes.getMandatoryExpressionNode("value") : flagValue,
      tagName, children, letBindings
    );
  }
}
