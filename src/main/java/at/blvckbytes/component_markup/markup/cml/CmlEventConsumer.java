/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public interface CmlEventConsumer {

  void onTagOpenBegin(InputView tagName);

  void onStringAttribute(InputView name, InputView value);

  void onTemplateLiteralAttribute(InputView name, TerminalNode value);

  void onLongAttribute(InputView name, InputView raw, long value);

  void onDoubleAttribute(InputView name, InputView raw, double value);

  void onBooleanAttribute(InputView name, InputView raw, boolean value);

  void onNullAttribute(InputView name, InputView raw);

  void onTagAttributeBegin(InputView name, int valueBeginPosition);

  void onTagAttributeEnd(InputView name);

  void onFlagAttribute(InputView name);

  void onTagOpenEnd(InputView tagName, boolean wasSelfClosing);

  void onText(InputView text);

  void onInterpolation(ExpressionNode expression, InputView raw);

  void onTagClose(@Nullable InputView tagName, int pointyPosition);

  void onInputEnd();

}
