/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class TagDefinition {

  public final TagClosing tagClosing;
  public final TagPriority tagPriority;

  protected TagDefinition(
    TagClosing tagClosing,
    TagPriority tagPriority
  ) {
    this.tagClosing = tagClosing;
    this.tagPriority = tagPriority;
  }

  public abstract boolean matchName(InputView tagName);

  public abstract @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  );
}
