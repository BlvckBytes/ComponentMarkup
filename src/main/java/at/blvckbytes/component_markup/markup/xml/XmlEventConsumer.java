package at.blvckbytes.component_markup.markup.xml;

import org.jetbrains.annotations.Nullable;

public interface XmlEventConsumer {

  void onCursorPosition(CursorPosition position);

  void onTagOpenBegin(String tagName);

  void onStringAttribute(String name, CursorPosition valueBeginPosition, String value);

  void onLongAttribute(String name, String raw, long value);

  void onDoubleAttribute(String name, String raw, double value);

  void onBooleanAttribute(String name, String raw, boolean value);

  void onTagAttributeBegin(String name);

  void onTagAttributeEnd(String name);

  void onFlagAttribute(String name);

  void onTagOpenEnd(String tagName, boolean wasSelfClosing);

  void onText(String text);

  void onInterpolation(String expression, CursorPosition valueBeginPosition);

  void onTagClose(@Nullable String tagName);

  void onInputEnd();

}
