package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import org.jetbrains.annotations.Nullable;

public class XmlEventJoiner implements XmlEventConsumer {

  private final StringBuilder eventsString = new StringBuilder();

  @Override
  public void onCursorPosition(CursorPosition position) {
    appendEvent(new CursorPositionEvent(position));
  }

  @Override
  public void onTagOpenBegin(String tagName) {
    appendEvent(new TagOpenBeginEvent(tagName));
  }

  @Override
  public void onStringAttribute(String name, CursorPosition valueBeginPosition, String value) {
    appendEvent(new StringAttributeEvent(name, valueBeginPosition, value));
  }

  @Override
  public void onLongAttribute(String name, String raw, long value) {
    appendEvent(new LongAttributeEvent(name, raw, value));
  }

  @Override
  public void onDoubleAttribute(String name, String raw, double value) {
    appendEvent(new DoubleAttributeEvent(name, raw, value));
  }

  @Override
  public void onBooleanAttribute(String name, String raw, boolean value) {
    appendEvent(new BooleanAttributeEvent(name, raw, value));
  }

  @Override
  public void onTagOpenEnd(String tagName, boolean wasSelfClosing) {
    appendEvent(new TagOpenEndEvent(tagName, wasSelfClosing));
  }

  @Override
  public void onTagAttributeBegin(String name) {
    appendEvent(new TagAttributeBeginEvent(name));
  }

  @Override
  public void onTagAttributeEnd(String name) {
    appendEvent(new TagAttributeEndEvent(name));
  }

  @Override
  public void onFlagAttribute(String name) {
    appendEvent(new FlagAttributeEvent(name));
  }

  @Override
  public void onText(String text) {
    appendEvent(new TextEvent(text));
  }

  @Override
  public void onInterpolation(String expression, CursorPosition valueBeginPosition) {
    appendEvent(new InterpolationEvent(expression, valueBeginPosition));
  }

  @Override
  public void onTagClose(@Nullable String tagName) {
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
