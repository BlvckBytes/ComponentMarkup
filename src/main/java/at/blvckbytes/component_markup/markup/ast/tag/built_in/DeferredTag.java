/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.MarkupInterpreter;
import at.blvckbytes.component_markup.platform.ComponentConstructor;
import at.blvckbytes.component_markup.platform.PlatformEntity;
import at.blvckbytes.component_markup.platform.SlotContext;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class DeferredTag extends TagDefinition {

  public DeferredTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("deferred", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    MarkupNode content = attributes.getMandatoryMarkupNode("content");

    return new DeferredNode<RendererParameter>(tagName, letBindings) {

      @Override
      public @Nullable List<Object> renderComponent(
        RendererParameter parameter,
        ComponentConstructor componentConstructor,
        InterpretationEnvironment environment,
        SlotContext slotContext,
        @Nullable PlatformEntity recipient
      ) {
        return MarkupInterpreter.interpret(
          componentConstructor, environment, recipient, slotContext, content
        ).unprocessedComponents;
      }

      @Override
      public RendererParameter createParameter(Interpreter interpreter) {
        return null;
      }
    };
  }
}
