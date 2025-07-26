package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public interface XmlEventConsumer {

  void onPosition(StringPosition position);

  void onTagOpenBegin(StringView tagName);

  void onStringAttribute(StringView name, StringView value);

  void onLongAttribute(StringView name, StringView raw, long value);

  void onDoubleAttribute(StringView name, StringView raw, double value);

  void onBooleanAttribute(StringView name, StringView raw, boolean value);

  void onTagAttributeBegin(StringView name);

  void onTagAttributeEnd(StringView name);

  void onFlagAttribute(StringView name);

  void onTagOpenEnd(StringView tagName, boolean wasSelfClosing);

  void onText(StringView text, EnumSet<SubstringFlag> flags);

  void onInterpolation(StringView expression);

  void onTagClose(@Nullable StringView tagName);

  void onInputEnd();

}
