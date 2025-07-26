package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;

import java.util.EnumSet;

public class TextEvent extends XmlEvent {

  public final StringView text;
  public final EnumSet<SubstringFlag> flags;

  public TextEvent(StringView text, EnumSet<SubstringFlag> flags) {
    this.text = text;
    this.flags = flags;
  }
}
