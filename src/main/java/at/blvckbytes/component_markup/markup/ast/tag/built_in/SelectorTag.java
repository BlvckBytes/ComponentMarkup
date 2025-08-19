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
import at.blvckbytes.component_markup.platform.coordinates.Coordinates;
import at.blvckbytes.component_markup.platform.coordinates.CoordinatesParseException;
import at.blvckbytes.component_markup.platform.coordinates.CoordinatesParser;
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
    ExpressionNode at = attributes.getOptionalExpressionNode("at");
    MarkupNode renderer = attributes.getOptionalMarkupNode("renderer");

    return new FunctionDrivenNode(tagName, interpreter -> {
      Coordinates coordinates = null;

      if (at != null) {
        String coordinatesString = interpreter.evaluateAsStringOrNull(at);

        if (coordinatesString != null) {
          InputView input = InputView.of(coordinatesString);

          try {
            coordinates = CoordinatesParser.parse(input);
          } catch (CoordinatesParseException parseException) {
            for (String line : ErrorScreen.make(at.getFirstMemberPositionProvider(), "Could not parse this coordinates-value"))
              LoggerProvider.log(Level.WARNING, line, false);

            LoggerProvider.log(Level.WARNING, "The following parse-error occurred:", false);

            for (String line : ErrorScreen.make(input.contents, parseException.position, parseException.getErrorMessage()))
              LoggerProvider.log(Level.WARNING, line, false);

            return null;
          }
        }
      }

      PlatformEntity recipient = interpreter.getRecipient();

      if (coordinates == null) {
        if (recipient == null) {
          for (String line : ErrorScreen.make(tagName, "Cannot execute a selector without a recipient or explicit coordinates"))
            LoggerProvider.log(Level.WARNING, line, false);

          return null;
        }

        coordinates = new Coordinates(recipient.x(), recipient.y(), recipient.z(), recipient.world());
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

      List<PlatformEntity> selectorResult = interpreter.getDataProvider().executeSelector(targetSelector, coordinates, recipient);

      interpreter.getEnvironment().setScopeVariable("selector_result", selectorResult);
      interpreter.getEnvironment().setScopeVariable("selector_origin", coordinates);

      if (renderer == null)
        return BuiltInTagRegistry.DEFAULT_SELECTOR_RENDERER;

      return renderer;
    });
  }
}
