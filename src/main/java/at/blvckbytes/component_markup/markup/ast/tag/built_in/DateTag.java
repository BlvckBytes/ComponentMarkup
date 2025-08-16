/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class DateTag extends TagDefinition {

  private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    return new FunctionDrivenNode(tagName, interpreter -> {
      Instant stamp = Instant.ofEpochMilli(
        value == null
          ? System.currentTimeMillis()
          : interpreter.evaluateAsLong(value)
      );

      ZoneId timeZone = SYSTEM_ZONE;

      if (zone != null) {
        String zoneString = interpreter.evaluateAsString(zone);
        try {
          timeZone = ZoneId.of(zoneString);
        } catch (Throwable e1) {
          try {
            timeZone = ZoneId.of(zoneString, ZoneId.SHORT_IDS);
          } catch (Throwable e2) {
            for (String line : ErrorScreen.make(zone.getFirstMemberPositionProvider(), "Invalid zone encountered"))
              LoggerProvider.log(Level.WARNING, line, false);
          }
        }
      }

      DateTimeFormatter formatter = DEFAULT_FORMATTER;

      if (format != null) {
        try {
          // TODO: Consider caching this formatter
          formatter = DateTimeFormatter.ofPattern(interpreter.evaluateAsString(format));
        } catch (Throwable e) {
          for (String line : ErrorScreen.make(format.getFirstMemberPositionProvider(), "Invalid format-pattern encountered"))
            LoggerProvider.log(Level.WARNING, line, false);
        }
      }

      return formatter.format(stamp.atZone(timeZone));
    });
  }
}
