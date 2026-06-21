/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.parser.token.TokenEmitter;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeparateTag extends TagDefinition {

  public SeparateTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("separate", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @Nullable TokenEmitter tokenEmitter,
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    MarkupList valueList = attributes.getMandatoryMarkupList("value");
    MarkupNode separator = attributes.getMandatoryMarkupNode("separator");
    MarkupNode empty = attributes.getOptionalMarkupNode("empty");

    return new FunctionDrivenNode(tagName, letBindings, interpreter -> {
      AtomicBoolean didHaveContent = new AtomicBoolean(false);

      for (MarkupNode markupNode : valueList.get(interpreter)) {
        interpreter.interpretIsolated(markupNode, outputBuilder -> {
          if (!outputBuilder.hasContent())
            return;

          if (didHaveContent.get()) {
            interpreter.interpret(separator);
            return;
          }

          didHaveContent.set(true);
        });
      }

      if (didHaveContent.get())
        return null;

      return empty;
    });
  }
}
