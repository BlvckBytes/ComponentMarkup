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
import at.blvckbytes.component_markup.platform.selector.SelectorParseException;
import at.blvckbytes.component_markup.platform.selector.SelectorParser;
import at.blvckbytes.component_markup.platform.selector.TargetSelector;
import at.blvckbytes.component_markup.platform.selector.TargetType;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class SelectorTag extends TagDefinition {

  public SelectorTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("selector", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode selector = attributes.getMandatoryExpressionNode("selector");
    MarkupNode renderer = attributes.getOptionalMarkupNode("renderer");

    return new FunctionDrivenNode(tagName, interpreter -> {
      PlatformEntity recipient = interpreter.getRecipient();

      // TODO: Why not just allow alternatively providing a location (coordinates/world)?
      if (recipient == null) {
        for (String line : ErrorScreen.make(tagName, "Cannot execute a selector without a recipient that's providing an origin"))
          LoggerProvider.log(Level.WARNING, line, false);

        return null;
      }

      InputView selectorString = InputView.of(interpreter.evaluateAsString(selector));

      TargetSelector targetSelector;

      try {
        targetSelector = SelectorParser.parse(selectorString);
      } catch (SelectorParseException parseException) {
        for (String line : ErrorScreen.make(selector.getFirstMemberPositionProvider(), "Could not parse this target-selector"))
          LoggerProvider.log(Level.WARNING, line, false);

        LoggerProvider.log(Level.WARNING, "Falling back to \"@p\"; the following parse-error occurred:", false);

        for (String line : ErrorScreen.make(selectorString.contents, parseException.position, parseException.getErrorMessage()))
          LoggerProvider.log(Level.WARNING, line, false);

        targetSelector = new TargetSelector(TargetType.NEAREST_PLAYER, selectorString, Collections.emptyList());
      }

      interpreter.getEnvironment().setScopeVariable("selector_result", recipient.executeSelector(targetSelector));
      interpreter.getEnvironment().setScopeVariable("selector_origin", recipient);

      if (renderer == null)
        return BuiltInTagRegistry.DEFAULT_SELECTOR_RENDERER;

      return renderer;
    });
  }
}
