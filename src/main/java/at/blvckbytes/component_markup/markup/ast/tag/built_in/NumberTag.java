/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TransformerNode;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.ValueInterpreter;
import at.blvckbytes.component_markup.markup.ast.node.ExpressionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NumberTag extends TagDefinition {

  private static final String ROUNDING_MODES_STRING = Arrays.stream(RoundingMode.values())
    .map(Enum::name)
    .collect(Collectors.joining(", "));

  private static final Map<String, Locale> LOCALE_BY_NAME_LOWER;

  static {
    LOCALE_BY_NAME_LOWER = new HashMap<>();

    for (Locale locale : Locale.getAvailableLocales()) {
      String localeName = locale.toString();

      for (int i = 0; i < localeName.length(); ++i) {
        if (!Character.isWhitespace(localeName.charAt(i))) {
          LOCALE_BY_NAME_LOWER.put(localeName.toLowerCase(), locale);
          break;
        }
      }
    }
  }

  public NumberTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals("number", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode value = attributes.getMandatoryExpressionNode("value");
    ExpressionNode absolute = attributes.getOptionalExpressionNode("absolute", "abs");
    ExpressionNode integer = attributes.getOptionalExpressionNode("integer", "int");
    ExpressionNode format = attributes.getOptionalExpressionNode("format");
    ExpressionNode rounding = format == null ? null : attributes.getOptionalExpressionNode("rounding");
    ExpressionNode locale = format == null ? null : attributes.getOptionalExpressionNode("locale");

    return new ExpressionDrivenNode(
      new TransformerNode(value, (input, environment) -> {
        ValueInterpreter valueInterpreter = environment.getValueInterpreter();
        Number number = environment.getValueInterpreter().asLongOrDouble(input);

        if (integer != null && valueInterpreter.asBoolean(ExpressionInterpreter.interpret(integer, environment)))
          number = number.intValue();

        if (absolute != null && valueInterpreter.asBoolean(ExpressionInterpreter.interpret(absolute, environment))) {
          if (number instanceof Double || number instanceof Float)
            number = Math.abs(number.doubleValue());
          else
            number = Math.abs(number.longValue());
        }

        if (format != null) {
          // This conversion is really not ideal, but I believe that it's more efficient on
          // the large-scale than to make the whole system operate on big-decimals. This kind of
          // precision should only matter when it comes to displaying values.
          BigDecimal bigDecimal;

          if (number instanceof Double)
            bigDecimal = new BigDecimal(Double.toString(number.doubleValue()));
          else if (number instanceof Float)
            bigDecimal = new BigDecimal(Float.toString(number.floatValue()));
          else
            bigDecimal = new BigDecimal(number.longValue());

          try {
            String pattern = valueInterpreter.asString(ExpressionInterpreter.interpret(format, environment));

            // TODO: Consider caching this format
            DecimalFormat decimalFormat = new DecimalFormat(pattern);

            if (rounding != null) {
              try {
                String modeName = valueInterpreter.asString(ExpressionInterpreter.interpret(rounding, environment));
                decimalFormat.setRoundingMode(RoundingMode.valueOf(modeName.toUpperCase()));
              } catch (Throwable e) {
                for (String line : ErrorScreen.make(rounding.getFirstMemberPositionProvider(), "Invalid rounding-mode encountered; choose one of " + ROUNDING_MODES_STRING))
                  LoggerProvider.log(Level.WARNING, line, false);
              }
            }

            if (locale != null) {
              String localeName = valueInterpreter.asString(ExpressionInterpreter.interpret(locale, environment));
              Locale formatLocale = LOCALE_BY_NAME_LOWER.get(localeName.toLowerCase());

              // TODO: Consider caching this format
              if (formatLocale != null)
                decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(formatLocale));

              else {
                for (String line : ErrorScreen.make(locale.getFirstMemberPositionProvider(), "Malformed locale-value encountered"))
                  LoggerProvider.log(Level.WARNING, line, false);
              }
            }

            return decimalFormat.format(bigDecimal);
          } catch (Throwable e) {
            for (String line : ErrorScreen.make(format.getFirstMemberPositionProvider(), "Invalid decimal-format encountered: " + e.getMessage()))
              LoggerProvider.log(Level.WARNING, line, false);
          }
        }

        return String.valueOf(number);
      })
    );
  }
}
