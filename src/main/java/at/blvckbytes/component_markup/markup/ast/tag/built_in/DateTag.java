/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.FormatDateWarning;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class DateTag extends TagDefinition {

  private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public DateTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("date", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode value = attributes.getOptionalExpressionNode("value");
    ExpressionNode zone = attributes.getOptionalExpressionNode("zone");
    ExpressionNode format = attributes.getOptionalExpressionNode("format");
    ExpressionNode locale = attributes.getOptionalExpressionNode("locale");

    return new FunctionDrivenNode(tagName, interpreter -> {
      String evaluatedFormat = format == null ? null : interpreter.evaluateAsStringOrNull(format);

      String formatString = evaluatedFormat == null ? DEFAULT_FORMAT : evaluatedFormat;
      String localeString = locale == null ? null : interpreter.evaluateAsStringOrNull(locale);
      String zoneString = zone == null ? null : interpreter.evaluateAsStringOrNull(zone);

      EnumSet<FormatDateWarning> warnings = EnumSet.noneOf(FormatDateWarning.class);
      String formattedString = interpreter.getEnvironment().interpretationPlatform.formatDate(
        formatString, localeString, zoneString,
        value == null ? System.currentTimeMillis() : interpreter.evaluateAsLong(value),
        warnings
      );

      if (warnings.contains(FormatDateWarning.INVALID_FORMAT) && evaluatedFormat != null) {
        for (String line : ErrorScreen.make(format.getFirstMemberPositionProvider(), "Invalid format-pattern encountered: \"" + formatString + "\""))
          LoggerProvider.log(Level.WARNING, line, false);
      }

      if (warnings.contains(FormatDateWarning.INVALID_LOCALE) && locale != null) {
        for (String line : ErrorScreen.make(locale.getFirstMemberPositionProvider(), "Malformed locale-value encountered: \"" + localeString + "\""))
          LoggerProvider.log(Level.WARNING, line, false);
      }

      if (warnings.contains(FormatDateWarning.INVALID_TIMEZONE) && zone != null) {
        for (String line : ErrorScreen.make(zone.getFirstMemberPositionProvider(), "Invalid zone encountered: \"" + zoneString + "\""))
          LoggerProvider.log(Level.WARNING, line, false);
      }

      return formattedString;
    });
  }
}
