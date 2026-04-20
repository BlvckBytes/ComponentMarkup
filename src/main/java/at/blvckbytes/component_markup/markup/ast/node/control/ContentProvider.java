/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ContentProvider {

  @Nullable List<MarkupNode> getContent();

}
