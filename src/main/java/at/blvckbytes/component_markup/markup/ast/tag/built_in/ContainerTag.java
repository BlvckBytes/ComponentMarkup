/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.parser.MarkupParseError;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ContainerTag extends TagDefinition {

  public ContainerTag() {
    super(TagClosing.INVARIANT, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("container", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    MarkupList valueList = attributes.getOptionalBoundFlagMarkupList();

    if (!valueList.isEmpty()) {
      if (!selfClosing)
        throw new MarkupParseException(tagName, MarkupParseError.CONTAINER_HAS_BOUND_FLAG_ATTRIBUTES_WHEN_NON_SELF_CLOSING);

      return new FunctionDrivenNode(tagName, interpreter -> {
        List<MarkupNode> valueChildren = valueList.get(interpreter);
        return new ContainerNode(tagName, valueChildren, letBindings);
      });
    }

    if (selfClosing)
      throw new MarkupParseException(tagName, MarkupParseError.CONTAINER_MISSING_BOUND_FLAG_ATTRIBUTES_WHEN_SELF_CLOSING);

    return new ContainerNode(tagName, children, letBindings);
  }
}
