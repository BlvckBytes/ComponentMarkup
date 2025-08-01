/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class DummyTag extends TagDefinition {

  public static final DummyTag INSTANCE = new DummyTag();

  private DummyTag() {
    super(TagClosing.INVARIANT, TagPriority.LOWEST);
  }

  @Override
  public boolean matchName(StringView tagName) {
    return true;
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ((InternalAttributeMap) attributes).markAllUsed();
    return new ContainerNode(tagName, children, letBindings);
  }
}
