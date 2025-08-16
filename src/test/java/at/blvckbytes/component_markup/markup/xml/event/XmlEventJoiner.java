/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class XmlEventJoiner implements XmlEventConsumer {

  private final List<XmlEvent> events = new ArrayList<>();

  @Override
  public void onTagOpenBegin(InputView tagName) {
    events.add(new TagOpenBeginEvent(tagName, tagName.buildString()));
  }

  @Override
  public void onStringAttribute(InputView name, InputView value) {
    events.add(new StringAttributeEvent(name, value, name.buildString(), value.buildString()));
  }

  @Override
  public void onTemplateLiteralAttribute(InputView name, TerminalNode value) {
    events.add(new TemplateLiteralAttributeEvent(name, value, name.buildString()));
  }

  @Override
  public void onLongAttribute(InputView name, InputView raw, long value) {
    events.add(new LongAttributeEvent(name, raw, value, name.buildString(), raw.buildString()));
  }

  @Override
  public void onDoubleAttribute(InputView name, InputView raw, double value) {
    events.add(new DoubleAttributeEvent(name, raw, value, name.buildString(), raw.buildString()));
  }

  @Override
  public void onBooleanAttribute(InputView name, InputView raw, boolean value) {
    events.add(new BooleanAttributeEvent(name, raw, value, name.buildString(), raw.buildString()));
  }

  @Override
  public void onNullAttribute(InputView name, InputView raw) {
    events.add(new NullAttributeEvent(name, raw, name.buildString(), raw.buildString()));
  }

  @Override
  public void onTagOpenEnd(InputView tagName, boolean wasSelfClosing) {
    events.add(new TagOpenEndEvent(tagName, wasSelfClosing));
  }

  @Override
  public void onTagAttributeBegin(InputView name, int valueBeginPosition) {
    events.add(new TagAttributeBeginEvent(name, valueBeginPosition, name.buildString()));
  }

  @Override
  public void onTagAttributeEnd(InputView name) {
    events.add(new TagAttributeEndEvent(name));
  }

  @Override
  public void onFlagAttribute(InputView name) {
    events.add(new FlagAttributeEvent(name, name.buildString()));
  }

  @Override
  public void onText(InputView text) {
    events.add(new TextEvent(text, text.buildString()));
  }

  @Override
  public void onInterpolation(ExpressionNode expression, InputView raw) {
    // Not going to test for the expression at this point, because that's
    // already taken care of plenty higher up the stack.
    events.add(new InterpolationEvent(raw, raw.buildString()));
  }

  @Override
  public void onTagClose(@Nullable InputView tagName, int pointyPosition) {
    events.add(new TagCloseEvent(tagName, pointyPosition, tagName == null ? null : tagName.buildString()));
  }

  @Override
  public void onInputEnd() {
    events.add(new InputEndEvent());
  }

  @Override
  public String toString() {
    StringBuilder eventsString = new StringBuilder();

    for (XmlEvent event : events) {
      if (eventsString.length() > 0)
        eventsString.append('\n');

      eventsString.append(Jsonifier.jsonify(event));
    }

    return eventsString.toString();
  }
}
