package at.blvckbytes.component_markup.markup.xml.event;

public class TextEvent extends XmlEvent {

  public final String text;

  public TextEvent(String text) {
    this.text = text;
  }
}
