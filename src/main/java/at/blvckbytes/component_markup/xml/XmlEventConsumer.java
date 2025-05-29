package at.blvckbytes.component_markup.xml;

public interface XmlEventConsumer {

  void onBeforeEventCursor(int nextCharIndex, int line, int column);

  void onTagOpenBegin(String tagName);

  void onStringAttribute(String name, String value);

  void onLongAttribute(String name, long value);

  void onDoubleAttribute(String name, double value);

  void onBooleanAttribute(String name, boolean value);

  void onNullAttribute(String name);

  void onTagAttributeBegin(String name);

  void onTagAttributeEnd(String name);

  void onTagOpenEnd(String name, boolean wasSelfClosing);

  void onText(String text);

  void onTagClose(String tagName);

  void onInputEnd();

}
