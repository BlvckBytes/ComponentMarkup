package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class XmlEventJoiner implements XmlEventConsumer {

  private final StringBuilder eventsString = new StringBuilder();

  @Override
  public void onPosition(StringPosition position) {
    appendEvent(new PositionEvent(position));
  }

  @Override
  public void onTagOpenBegin(StringView tagName) {
    appendEvent(new TagOpenBeginEvent(tagName));
  }

  @Override
  public void onStringAttribute(StringView name, StringView value) {
    appendEvent(new StringAttributeEvent(name, value));
  }

  @Override
  public void onLongAttribute(StringView name, StringView raw, long value) {
    appendEvent(new LongAttributeEvent(name, raw, value));
  }

  @Override
  public void onDoubleAttribute(StringView name, StringView raw, double value) {
    appendEvent(new DoubleAttributeEvent(name, raw, value));
  }

  @Override
  public void onBooleanAttribute(StringView name, StringView raw, boolean value) {
    appendEvent(new BooleanAttributeEvent(name, raw, value));
  }

  @Override
  public void onTagOpenEnd(StringView tagName, boolean wasSelfClosing) {
    appendEvent(new TagOpenEndEvent(tagName, wasSelfClosing));
  }

  @Override
  public void onTagAttributeBegin(StringView name) {
    appendEvent(new TagAttributeBeginEvent(name));
  }

  @Override
  public void onTagAttributeEnd(StringView name) {
    appendEvent(new TagAttributeEndEvent(name));
  }

  @Override
  public void onFlagAttribute(StringView name) {
    appendEvent(new FlagAttributeEvent(name));
  }

  @Override
  public void onText(StringView text, EnumSet<SubstringFlag> flags) {
    appendEvent(new TextEvent(text, flags));
  }

  @Override
  public void onInterpolation(StringView expression) {
    appendEvent(new InterpolationEvent(expression));
  }

  @Override
  public void onTagClose(@Nullable StringView tagName) {
    appendEvent(new TagCloseEvent(tagName));
  }

  @Override
  public void onInputEnd() {
    appendEvent(new InputEndEvent());
  }

  @Override
  public String toString() {
    return eventsString.toString();
  }

  private void appendEvent(XmlEvent event) {
    if (eventsString.length() > 0)
      eventsString.append('\n');

    eventsString.append(Jsonifier.jsonify(event));
  }
}
