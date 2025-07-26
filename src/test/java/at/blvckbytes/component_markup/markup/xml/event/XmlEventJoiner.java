package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.util.StringPosition;
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
  public void onTagOpenEnd(StringView tagName, boolean wasSelfClosing) {
    events.add(new TagOpenEndEvent(tagName, wasSelfClosing));
  }

  @Override
  public void onTagAttributeBegin(StringView name, StringPosition valueBeginPosition) {
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
  public void onInterpolation(StringView expression) {
    events.add(new InterpolationEvent(expression, expression.buildString()));
  }

  @Override
  public void onTagClose(@Nullable StringView tagName, StringPosition pointyPosition) {
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
