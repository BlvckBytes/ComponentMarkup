/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.platform.PlatformEntity;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class PlayerNameTag extends TagDefinition {

  public PlayerNameTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("player-name", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode displayName = attributes.getOptionalExpressionNode("display-name");
    MarkupNode renderer = attributes.getOptionalMarkupNode("renderer");

    return new FunctionDrivenNode(tagName, interpreter -> {
      PlatformEntity recipient = interpreter.getRecipient();

      if (recipient == null) {
        for (String line : ErrorScreen.make(tagName, "Cannot get the player's name when not provided with a recipient"))
          LoggerProvider.log(Level.WARNING, line, false);

        return null;
      }

      String name = recipient.name;

      if (displayName != null && interpreter.evaluateAsBoolean(displayName))
        name = recipient.displayName;

      if (renderer == null)
        return name;

      interpreter.getEnvironment().setScopeVariable("player_name", name);

      return renderer;
    });
  }
}
