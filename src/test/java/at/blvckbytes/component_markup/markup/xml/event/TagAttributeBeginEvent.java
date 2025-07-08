package at.blvckbytes.component_markup.markup.xml.event;

public class TagAttributeBeginEvent extends XmlEvent {

  public final String name;

  public TagAttributeBeginEvent(String name) {
    this.name = name;
  }
}
