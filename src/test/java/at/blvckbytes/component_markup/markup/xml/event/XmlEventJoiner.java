/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class XmlEventJoiner implements XmlEventConsumer {

  private final List<XmlEvent> events = new ArrayList<>();

  @Override
  public void onTagOpenBegin(StringView tagName) {
    events.add(new TagOpenBeginEvent(tagName, tagName.buildString()));
  }

  @Override
  public void onStringAttribute(StringView name, StringView value) {
    events.add(new StringAttributeEvent(name, value, name.buildString(), value.buildString()));
  }

  @Override
  public void onLongAttribute(StringView name, StringView raw, long value) {
    events.add(new LongAttributeEvent(name, raw, value, name.buildString(), raw.buildString()));
  }

  @Override
  public void onDoubleAttribute(StringView name, StringView raw, double value) {
    events.add(new DoubleAttributeEvent(name, raw, value, name.buildString(), raw.buildString()));
  }

  @Override
  public void onBooleanAttribute(StringView name, StringView raw, boolean value) {
    events.add(new BooleanAttributeEvent(name, raw, value, name.buildString(), raw.buildString()));
  }

  @Override
  public void onNullAttribute(StringView name, StringView raw) {
    events.add(new NullAttributeEvent(name, raw, name.buildString(), raw.buildString()));
  }

  @Override
  public void onTagOpenEnd(StringView tagName, boolean wasSelfClosing) {
    events.add(new TagOpenEndEvent(tagName, wasSelfClosing));
  }

  @Override
  public void onTagAttributeBegin(StringView name, int valueBeginPosition) {
    events.add(new TagAttributeBeginEvent(name, valueBeginPosition, name.buildString()));
  }

  @Override
  public void onTagAttributeEnd(StringView name) {
    events.add(new TagAttributeEndEvent(name));
  }

  @Override
  public void onFlagAttribute(StringView name) {
    events.add(new FlagAttributeEvent(name, name.buildString()));
  }

  @Override
  public void onText(StringView text) {
    events.add(new TextEvent(text, text.buildString()));
  }

  @Override
  public void onInterpolation(ExpressionNode expression, StringView raw) {
    // Not going to test for the expression at this point, because that's
    // already taken care of plenty higher up the stack.
    events.add(new InterpolationEvent(raw, raw.buildString()));
  }

  @Override
  public void onTagClose(@Nullable StringView tagName, int pointyPosition) {
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
