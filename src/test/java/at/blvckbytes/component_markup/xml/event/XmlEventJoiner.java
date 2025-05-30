package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.xml.XmlEventConsumer;

public class XmlEventJoiner implements XmlEventConsumer {

  private final StringBuilder eventsString = new StringBuilder();

  @Override
  public void onBeforeEventCursor(int offset, int line, int column) {
    appendEvent(new BeforeEventCursorEvent(offset, line, column));
  }

  @Override
  public void onTagOpenBegin(String tagName) {
    appendEvent(new TagOpenBeginEvent(tagName));
  }

  @Override
  public void onStringAttribute(String name, String value) {
    appendEvent(new StringAttributeEvent(name, value));
  }

  @Override
  public void onLongAttribute(String name, long value) {
    appendEvent(new LongAttributeEvent(name, value));
  }

  @Override
  public void onDoubleAttribute(String name, double value) {
    appendEvent(new DoubleAttributeEvent(name, value));
  }

  @Override
  public void onBooleanAttribute(String name, boolean value) {
    appendEvent(new BooleanAttributeEvent(name, value));
  }

  @Override
  public void onNullAttribute(String name) {
    appendEvent(new NullAttributeEvent(name));
  }

  @Override
  public void onTagOpenEnd(String name, boolean wasSelfClosing) {
    appendEvent(new TagOpenEndEvent(name, wasSelfClosing));
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
  public void onText(String text) {
    appendEvent(new TextEvent(text));
  }

  @Override
  public void onInterpolation(String expression) {
    appendEvent(new InterpolationEvent(expression));
  }

  @Override
  public void onTagClose(String tagName) {
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

    eventsString.append(event);
  }
}
