/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.duration;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.TemporaryMemberEnvironment;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DurationTag extends TagDefinition {

  public DurationTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("duration", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode units = attributes.getMandatoryExpressionNode("units");
    MarkupNode unitRenderer = attributes.getOptionalMarkupNode("unit-renderer");
    MarkupNode separator = attributes.getOptionalMarkupNode("separator");
    ExpressionNode zeroes = attributes.getOptionalExpressionNode("zeroes");

    ExpressionNode flagValue = attributes.getOptionalBoundFlagExpressionNode();
    ExpressionNode value = flagValue == null ? attributes.getMandatoryExpressionNode("value") : flagValue;

    return new FunctionDrivenNode(tagName, interpreter -> {
      TemporaryMemberEnvironment environment = interpreter.getEnvironment();

      double duration = interpreter.evaluateAsDouble(value);
      List<DurationUnit> requestedUnits = parseUnitsString(interpreter, units);
      boolean keepZeroes = zeroes != null && interpreter.evaluateAsBoolean(zeroes);

      requestedUnits.sort((a, b) -> Long.compare(b.milliseconds, a.milliseconds));

      double[] unitValues = new double[requestedUnits.size()];
      double remaining = duration;

      for (int requestedUnitIndex = 0; requestedUnitIndex < requestedUnits.size(); ++requestedUnitIndex) {
        DurationUnit requestedUnit = requestedUnits.get(requestedUnitIndex);
        double unitValue = remaining / requestedUnit.milliseconds;
        boolean isLastUnit = requestedUnitIndex == requestedUnits.size() - 1;

        if (!isLastUnit)
          unitValue = Math.floor(unitValue);

        else {
          // Account for precision-oddities; better safe than sorry!
          if (unitValue < .001)
            unitValue = 0;

          // Without an explicit unit-renderer, we always discard the fractional part, seeing how
          // it requires proper formatting, which we are not going to take care of (separation
          // of concerns - simply employ the number-tag in the unit-renderer).
          if (unitRenderer == null)
            unitValue = Math.floor(unitValue);
        }

        while (remaining > requestedUnit.milliseconds)
          remaining -= requestedUnit.milliseconds;

        unitValues[requestedUnitIndex] = unitValue;
      }

      boolean didEmit = false;

      for (int requestedUnitIndex = 0; requestedUnitIndex < requestedUnits.size(); ++requestedUnitIndex) {
        DurationUnit requestedUnit = requestedUnits.get(requestedUnitIndex);
        double unitValue = unitValues[requestedUnitIndex];

        if (unitValue == 0 && !keepZeroes)
          continue;

        if (didEmit)
          interpreter.interpret(separator == null ? new TextNode(InputView.EMPTY, " ") : separator, null);

        didEmit = true;

        if (unitRenderer == null) {
          interpreter.interpret(new TextNode(InputView.EMPTY, String.valueOf((int) unitValue) + requestedUnit.character), null);
          continue;
        }

        interpreter.interpret(unitRenderer, () -> {
          environment.setScopeVariable("unit", String.valueOf(requestedUnit.character));

          boolean isFractional = unitValue != Math.floor(unitValue);

          environment.setScopeVariable("is_fractional", isFractional);

          // Prefer integers if there's no fractional part, as they stringify more desirable by default.
          if (!isFractional) {
            environment.setScopeVariable("value", (int) unitValue);
            return;
          }

          environment.setScopeVariable("value", unitValue);
        });
      }

      return null;
    });
  }

  private List<DurationUnit> parseUnitsString(Interpreter<?, ?> interpreter, ExpressionNode units) {
    List<DurationUnit> result = new ArrayList<>();

    String unitsString = interpreter.evaluateAsString(units);

    for (int charIndex = 0; charIndex < unitsString.length(); ++charIndex) {
      char c = unitsString.charAt(charIndex);

      if (Character.isWhitespace(c))
        continue;

      DurationUnit unit = DurationUnit.fromChar(c);

      if (unit == null) {
        interpreter.getLogger().logErrorScreen(units.getFirstMemberPositionProvider(), "Could not parse this units-string (unknown unit \"" + c + "\"); falling back to \"s\"");
        return Collections.singletonList(DurationUnit.SECONDS);
      }

      result.add(unit);
    }

    return result;
  }
}
