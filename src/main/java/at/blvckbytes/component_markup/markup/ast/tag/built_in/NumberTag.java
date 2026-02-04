/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.FormatNumberWarning;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NumberTag extends TagDefinition {

  public NumberTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("number", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode flagValue = attributes.getOptionalBoundFlagExpressionNode();
    ExpressionNode value = flagValue == null ? attributes.getMandatoryExpressionNode("value") : flagValue;

    ExpressionNode absolute = attributes.getOptionalExpressionNode("absolute", "abs");
    ExpressionNode integer = attributes.getOptionalExpressionNode("integer", "int");
    ExpressionNode format = attributes.getOptionalExpressionNode("format");
    ExpressionNode rounding = format == null ? null : attributes.getOptionalExpressionNode("rounding");
    ExpressionNode locale = format == null ? null : attributes.getOptionalExpressionNode("locale");

    return new FunctionDrivenNode(tagName, interpreter -> {
      Number number = interpreter.evaluateAsLongOrDouble(value);

      if (integer != null && interpreter.evaluateAsBoolean(integer))
        number = number.intValue();

      if (absolute != null && interpreter.evaluateAsBoolean(absolute)) {
        if (number instanceof Double || number instanceof Float)
          number = Math.abs(number.doubleValue());
        else
          number = Math.abs(number.longValue());
      }

      String formatString;

      if (format != null && (formatString = interpreter.evaluateAsStringOrNull(format)) != null) {
        String roundingString = rounding == null ? null : interpreter.evaluateAsStringOrNull(rounding);
        String localeString = locale == null ? null : interpreter.evaluateAsStringOrNull(locale);

        EnumSet<FormatNumberWarning> warnings = EnumSet.noneOf(FormatNumberWarning.class);
        String formattedString = interpreter.getEnvironment().interpretationPlatform.formatNumber(formatString, roundingString, localeString, number, warnings);

        if (warnings.contains(FormatNumberWarning.INVALID_FORMAT))
          interpreter.getLogger().logErrorScreen(format.getFirstMemberPositionProvider(), "Invalid number-format pattern encountered: \"" + formatString + "\"");

        if (warnings.contains(FormatNumberWarning.INVALID_ROUNDING_MODE) && rounding != null)
          interpreter.getLogger().logErrorScreen(rounding.getFirstMemberPositionProvider(), "Invalid rounding-mode encountered: \"" + roundingString + "\"");

        if (warnings.contains(FormatNumberWarning.INVALID_LOCALE) && locale != null)
          interpreter.getLogger().logErrorScreen(locale.getFirstMemberPositionProvider(), "Invalid locale-value encountered: \"" + localeString + "\"");

        return formattedString;
      }

      return String.valueOf(number);
    });
  }
}
