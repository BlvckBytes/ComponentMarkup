/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ContentNode extends MarkupNode {

  public final ThreadLocal<@Nullable ContentProvider> contentProvider;

  public ContentNode(InputView positionProvider) {
    super(positionProvider, null, null);

    this.contentProvider = ThreadLocal.withInitial(() -> null);
  }

  public void forEachContentNode(Consumer<MarkupNode> handler) {
    ContentProvider currentContentProvider = contentProvider.get();

    if (currentContentProvider == null)
      return;

    List<MarkupNode> currentContentNodes = currentContentProvider.getContent();

    if (currentContentNodes != null)
      currentContentNodes.forEach(handler);
  }
}
