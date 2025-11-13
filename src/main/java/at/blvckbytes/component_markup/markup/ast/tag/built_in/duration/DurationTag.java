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
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode value = attributes.getMandatoryExpressionNode("value");
    ExpressionNode units = attributes.getMandatoryExpressionNode("units");
    MarkupNode unitRenderer = attributes.getOptionalMarkupNode("unit-renderer");
    MarkupNode separator = attributes.getOptionalMarkupNode("separator");
    ExpressionNode zeroes = attributes.getOptionalExpressionNode("zeroes");

    return new FunctionDrivenNode(tagName, interpreter -> {
      TemporaryMemberEnvironment environment = interpreter.getEnvironment();

      long duration = interpreter.evaluateAsLong(value);
      List<DurationUnit> requestedUnits = parseUnitsString(interpreter, units);
      boolean keepZeroes = zeroes != null && interpreter.evaluateAsBoolean(zeroes);

      requestedUnits.sort((a, b) -> Long.compare(b.milliseconds, a.milliseconds));

      long[] unitValues = new long[requestedUnits.size()];
      long remaining = duration;

      for (int requestedUnitIndex = 0; requestedUnitIndex < requestedUnits.size(); ++requestedUnitIndex) {
        DurationUnit requestedUnit = requestedUnits.get(requestedUnitIndex);
        long unitValue = remaining / requestedUnit.milliseconds;
        remaining %= requestedUnit.milliseconds;

        unitValues[requestedUnitIndex] = unitValue;
      }

      boolean didEmit = false;

      for (int requestedUnitIndex = 0; requestedUnitIndex < requestedUnits.size(); ++requestedUnitIndex) {
        DurationUnit requestedUnit = requestedUnits.get(requestedUnitIndex);
        long unitValue = unitValues[requestedUnitIndex];

        if (unitValue == 0 && !keepZeroes)
          continue;

        if (didEmit)
          interpreter.interpret(separator == null ? new TextNode(InputView.EMPTY, " ") : separator, null);

        didEmit = true;

        if (unitRenderer == null) {
          interpreter.interpret(new TextNode(InputView.EMPTY, String.valueOf(unitValue) + requestedUnit.character), null);
          continue;
        }

        interpreter.interpret(unitRenderer, () -> {
          environment.setScopeVariable("value", unitValue);
          environment.setScopeVariable("unit", String.valueOf(requestedUnit.character));
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
