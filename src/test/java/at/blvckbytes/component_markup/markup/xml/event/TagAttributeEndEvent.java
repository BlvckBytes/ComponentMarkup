package at.blvckbytes.component_markup.markup.xml.event;

public class TagAttributeEndEvent extends XmlEvent {

  public final String name;

  public TagAttributeEndEvent(String name) {
    this.name = name;
  }
}
